package com.kahavanu.data.goals

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.goals.local.GoalLogDao
import com.kahavanu.data.goals.sync.GoalsSyncScheduler
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.repository.GoalsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DefaultGoalsRepository"
private const val USERS_COLLECTION = "users"
private const val GOAL_LOGS_COLLECTION = "goalLogs"

@Singleton
class DefaultGoalsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val goalLogDao: GoalLogDao,
    private val syncScheduler: GoalsSyncScheduler,
    @ApplicationContext private val appContext: Context,
) : GoalsRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private var goalsListener: ListenerRegistration? = null
    private var listenerUserId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            stopRealtimeListeners()
            return@AuthStateListener
        }
        startRealtimeListeners(uid)
        syncScheduler.enqueue()
    }

    init {
        syncScheduler.enqueue()
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { startRealtimeListeners(it) }
    }

    override fun observeGoals(): Flow<List<GoalEntry>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return goalLogDao.observeGoals(uid).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addGoal(goal: GoalEntry): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val entity = goal.toEntity(uid)
        val localId = goalLogDao.insert(entity)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(Unit)
        }

        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(GOAL_LOGS_COLLECTION)
            .document(entity.clientId)
            .set(entity.toFirestoreMap())
            .awaitResult()

        return if (remoteResult.isSuccess) {
            goalLogDao.markSynced(localId, entity.clientId)
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun updateGoal(goal: GoalEntry): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = goalLogDao.getByClientId(goal.id)
            ?: goalLogDao.getByRemoteId(goal.id)
        val entity = goal.toEntity(uid).copy(
            localId = existing?.localId ?: 0L,
            remoteId = existing?.remoteId,
            clientId = existing?.clientId ?: goal.id,
            isSynced = false,
        )
        goalLogDao.upsert(entity)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(Unit)
        }

        val remoteId = entity.remoteId ?: entity.clientId
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(GOAL_LOGS_COLLECTION)
            .document(remoteId)
            .set(entity.toFirestoreMap(), SetOptions.merge())
            .awaitResult()

        return Result.success(Unit)
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = goalLogDao.getByClientId(id) ?: goalLogDao.getByRemoteId(id)
        existing?.let { entity ->
            goalLogDao.deleteByLocalIds(listOf(entity.localId))
            val remoteId = entity.remoteId ?: return Result.success(Unit)
            if (isOnline()) {
                firestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .collection(GOAL_LOGS_COLLECTION)
                    .document(remoteId)
                    .delete()
                    .awaitResult()
            } else {
                syncScheduler.enqueue()
            }
        }
        return Result.success(Unit)
    }

    private fun startRealtimeListeners(uid: String) {
        if (listenerUserId == uid) return
        stopRealtimeListeners()
        listenerUserId = uid

        goalsListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(GOAL_LOGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Goals listener error", error)
                    return@addSnapshotListener
                }
                snapshot ?: return@addSnapshotListener
                repositoryScope.launch {
                    snapshot.documentChanges.forEach { change ->
                        val doc = change.document
                        val remoteId = doc.id
                        val existing = goalLogDao.getByRemoteId(remoteId) ?: goalLogDao.getByClientId(remoteId)
                        when (change.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                val entity = doc.toGoalLogEntity(uid, remoteId, existing?.localId ?: 0L)
                                goalLogDao.upsert(entity)
                            }
                            DocumentChange.Type.REMOVED -> {
                                goalLogDao.deleteByRemoteId(remoteId)
                            }
                        }
                    }
                }
            }
    }

    private fun stopRealtimeListeners() {
        goalsListener?.remove()
        goalsListener = null
        listenerUserId = null
    }

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
