package com.kahavanu.data.sieve

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.sieve.local.SmsSenderDao
import com.kahavanu.data.sieve.local.SmsSenderEntity
import com.kahavanu.data.sieve.sync.SmsSenderSyncScheduler
import com.kahavanu.domain.model.SmsSender
import com.kahavanu.domain.repository.SmsSenderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSmsSenderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val smsSenderDao: SmsSenderDao,
    private val syncScheduler: SmsSenderSyncScheduler,
    @ApplicationContext private val appContext: Context,
) : SmsSenderRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private var smsSendersListener: ListenerRegistration? = null
    private var listenerUserId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            stopRealtimeListeners()
            return@AuthStateListener
        }
        repositoryScope.launch {
            ensureDefaultSenders(uid)
        }
        startRealtimeListeners(uid)
        syncScheduler.enqueue()
    }

    init {
        syncScheduler.enqueue()
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { uid ->
            repositoryScope.launch {
                ensureDefaultSenders(uid)
            }
            startRealtimeListeners(uid)
        }
    }

    private suspend fun ensureDefaultSenders(uid: String) {
        val existing = smsSenderDao.getSendersForUser(uid)
        if (existing.isNotEmpty()) return

        val defaults = listOf("PickMe", "Commercial Bank", "Keells")
        val now = System.currentTimeMillis()
        defaults.forEach { name ->
            val entity = SmsSenderEntity(
                userId = uid,
                senderName = name,
                isEnabled = true,
                createdAtEpochMillis = now,
                clientId = UUID.randomUUID().toString(),
                isSynced = false,
                isDeleted = false,
                updatedAtEpochMillis = now
            )
            smsSenderDao.insert(entity)
        }
        syncScheduler.enqueue()
    }

    override fun observeAuthorizedSenders(): Flow<List<SmsSender>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return smsSenderDao.observeSenders(uid)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addAuthorizedSender(senderName: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val now = System.currentTimeMillis()
        val clientId = UUID.randomUUID().toString()
        val localEntity = SmsSenderEntity(
            userId = uid,
            senderName = senderName,
            isEnabled = true,
            createdAtEpochMillis = now,
            clientId = clientId,
            isSynced = false,
            isDeleted = false,
            updatedAtEpochMillis = now,
        )
        val localId = smsSenderDao.insert(localEntity)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(Unit)
        }

        val data = localEntity.toFirestoreMap()
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SMS_SENDERS_COLLECTION)
            .document(clientId)
            .set(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            smsSenderDao.markSynced(localId, clientId)
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun toggleSenderEnabled(senderId: String, isEnabled: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = smsSenderDao.getByRemoteId(senderId) ?: smsSenderDao.getByClientId(senderId)
            ?: return Result.failure(IllegalArgumentException("Sender not found"))

        val now = System.currentTimeMillis()
        val updated = existing.copy(
            isEnabled = isEnabled,
            isSynced = false,
            updatedAtEpochMillis = now
        )
        smsSenderDao.upsert(updated)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(Unit)
        }

        val data = updated.toFirestoreMap()
        val docId = updated.remoteId ?: updated.clientId
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SMS_SENDERS_COLLECTION)
            .document(docId)
            .set(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            smsSenderDao.markSynced(updated.localId, docId)
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun deleteAuthorizedSender(senderId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = smsSenderDao.getByRemoteId(senderId) ?: smsSenderDao.getByClientId(senderId)
            ?: return Result.failure(IllegalArgumentException("Sender not found"))

        val now = System.currentTimeMillis()
        val deleted = existing.copy(
            isDeleted = true,
            isSynced = false,
            updatedAtEpochMillis = now
        )
        smsSenderDao.upsert(deleted)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(Unit)
        }

        val docId = deleted.remoteId ?: deleted.clientId
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SMS_SENDERS_COLLECTION)
            .document(docId)
            .delete()
            .awaitResult()

        return if (remoteResult.isSuccess) {
            smsSenderDao.deleteByLocalIds(listOf(deleted.localId))
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun startRealtimeListeners(uid: String) {
        if (listenerUserId == uid && smsSendersListener != null) {
            return
        }
        stopRealtimeListeners()
        listenerUserId = uid

        smsSendersListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SMS_SENDERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("SmsSenderRepository", "SMS senders listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (change in snapshot.documentChanges) {
                        handleSmsSenderChange(uid, change)
                    }
                }
            }
    }

    private fun stopRealtimeListeners() {
        smsSendersListener?.remove()
        smsSendersListener = null
        listenerUserId = null
    }

    private suspend fun handleSmsSenderChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remoteId = doc.id
        val clientId = doc.getString("clientId") ?: remoteId
        when (change.type) {
            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                val localByRemote = smsSenderDao.getByRemoteId(remoteId)
                val localByClientId = if (localByRemote == null) {
                    smsSenderDao.getByClientId(clientId)
                } else {
                    null
                }
                val remoteEntity = doc.toSmsSenderEntity(uid, remoteId, 0L)
                val local = localByRemote ?: localByClientId
                val entity = remoteEntity.copy(localId = local?.localId ?: 0L)
                if (local == null || entity.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                    smsSenderDao.upsert(entity)
                }
            }
            DocumentChange.Type.REMOVED -> {
                smsSenderDao.deleteByRemoteId(remoteId)
            }
        }
    }
}

private const val USERS_COLLECTION = "users"
private const val SMS_SENDERS_COLLECTION = "smsSenders"
