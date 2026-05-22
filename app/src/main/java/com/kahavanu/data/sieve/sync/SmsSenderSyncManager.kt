package com.kahavanu.data.sieve.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.sieve.toSmsSenderEntity
import com.kahavanu.data.sync.FirebaseSyncManager
import com.kahavanu.data.sync.SyncState
import com.kahavanu.data.sync.awaitResultDocRef
import com.kahavanu.data.sync.awaitResultQuery
import com.kahavanu.data.sync.awaitResultVoid
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSenderSyncManager @Inject constructor(
    @ApplicationContext context: Context,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    private val database: AppDatabase,
) : FirebaseSyncManager(context, firestore, auth) {

    override suspend fun sync(): Result<Unit> {
        _syncState.value = SyncState.Syncing

        val uid = getCurrentUserId()
            ?: return Result.failure<Unit>(Exception("Not authenticated")).also {
                _syncState.value = SyncState.Error(Exception("Not authenticated"))
            }

        return try {
            pushPendingSenders(uid)
            pullRemoteSenders(uid)
            updateLastSyncTimestamp()
            _syncState.value = SyncState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            Result.failure(e)
        }
    }

    private suspend fun pushPendingSenders(uid: String) {
        val dao = database.smsSenderDao()
        val pending = dao.getUnsynced(uid)
        for (entity in pending) {
            try {
                val collection = firestore.collection("users").document(uid).collection("smsSenders")
                val data = entity.toFirestoreMap()
                val docId = entity.remoteId ?: entity.clientId
                collection.document(docId)
                    .set(data, SetOptions.merge())
                    .awaitResultVoid()
                    .getOrThrow()
                dao.markSynced(entity.localId, docId)
            } catch (e: Exception) {
                android.util.Log.w("SmsSenderSyncManager", "Failed to push SMS sender localId=${entity.localId}", e)
            }
        }
    }

    private suspend fun pullRemoteSenders(uid: String) {
        val dao = database.smsSenderDao()
        val snapshot = firestore
            .collection("users").document(uid).collection("smsSenders")
            .get()
            .awaitResultQuery()
            .getOrNull() ?: return

        snapshot.documents.forEach { doc ->
            val remoteId = doc.id
            val clientId = doc.getString("clientId") ?: remoteId
            val senderName = doc.getString("senderName") ?: ""
            val existing = dao.getByRemoteId(remoteId)
                ?: dao.getByClientId(clientId)
                ?: if (senderName.isNotBlank()) dao.getByName(uid, senderName) else null

            val entity = doc.toSmsSenderEntity(uid, remoteId, localId = existing?.localId ?: 0L)
            if (existing == null || existing.remoteId == null || entity.updatedAtEpochMillis > existing.updatedAtEpochMillis) {
                dao.upsert(entity)
            }
        }
    }
}
