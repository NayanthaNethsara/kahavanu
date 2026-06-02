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
import com.kahavanu.data.goals.local.GoalAdjustmentLogDao
import com.kahavanu.data.goals.local.GoalAdjustmentLogEntity
import com.kahavanu.data.goals.local.GoalLogDao
import com.kahavanu.data.goals.local.GoalLogEntity
import com.kahavanu.data.goals.sync.GoalsSyncScheduler
import com.kahavanu.domain.model.GoalAdjustmentLog
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.notifications.AppNotifier
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
    private val goalAdjustmentLogDao: GoalAdjustmentLogDao,
    private val syncScheduler: GoalsSyncScheduler,
    private val appNotifier: AppNotifier,
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

    override fun observeAdjustmentLogs(goalClientId: String): Flow<List<GoalAdjustmentLog>> =
        goalAdjustmentLogDao.observeLogsForGoal(goalClientId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeGoals(): Flow<List<GoalEntry>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return goalLogDao.observeGoals(uid).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addGoal(goal: GoalEntry): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        // First non-completed goal becomes the active one automatically; the rest go
        // to the bottom of the backlog. This keeps the "exactly one active" invariant
        // without forcing the user to pick an active goal during setup.
        val existing = goalLogDao.getActiveGoalsForUser(uid)
        val hasActive = existing.any { it.isActive }
        val maxBacklogPriority = existing.filter { !it.isActive }.maxOfOrNull { it.priority } ?: -1
        val prepared = goal.copy(
            isActive = if (!goal.isCompleted) !hasActive else false,
            priority = if (!goal.isCompleted && hasActive) maxBacklogPriority + 1 else 0,
        )

        val entity = prepared.toEntity(uid)
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

    override suspend fun adjustSavedAmount(goalId: String, delta: Double): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        val existing = goalLogDao.getByClientId(goalId) ?: goalLogDao.getByRemoteId(goalId)
            ?: return Result.failure(IllegalArgumentException("Goal not found: $goalId"))
        val newAmount = (existing.currentAmount + delta).coerceAtLeast(0.0)
        val justCompleted = !existing.isCompleted &&
            existing.targetAmount > 0.0 && newAmount >= existing.targetAmount
        val updated = existing.toDomain().copy(
            currentAmount = newAmount,
            isCompleted = existing.isCompleted || justCompleted,
            // A completed goal releases the single active slot so the next goal can take over.
            isActive = if (justCompleted) false else existing.isActive,
        )
        goalAdjustmentLogDao.insert(
            GoalAdjustmentLogEntity(
                goalClientId = existing.clientId,
                userId = uid,
                delta = delta,
                newAmount = newAmount,
            )
        )
        val result = updateGoal(updated)

        if (justCompleted) {
            // If the finished goal was the active one, promote the top backlog goal so the
            // user is never left without an active goal while the backlog still has items.
            if (existing.isActive) {
                // getActiveGoalsForUser excludes completed goals, so the just-finished goal
                // is already out of this list.
                val candidates = goalLogDao.getActiveGoalsForUser(uid)
                    .filter { it.clientId != existing.clientId }
                val next = candidates.minByOrNull { it.priority }
                if (next != null) {
                    promoteToActive(uid, next, others = candidates.filter { it.clientId != next.clientId })
                }
            }
            appNotifier.notifyGoalCompleted(existing.title)
        }
        return result
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = goalLogDao.getByClientId(id) ?: goalLogDao.getByRemoteId(id)
        existing?.let { entity ->
            goalLogDao.deleteByLocalIds(listOf(entity.localId))
            // If the deleted goal was active, promote the highest-priority backlog goal
            // so the user is never left without an active goal while backlog still has items.
            if (entity.isActive) {
                val remaining = goalLogDao.getActiveGoalsForUser(uid)
                    .filter { it.clientId != entity.clientId }
                val next = remaining.minByOrNull { it.priority }
                if (next != null) {
                    promoteToActive(uid, next, others = remaining.filter { it.clientId != next.clientId })
                }
            }
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

    override suspend fun setActiveGoal(goalId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        val target = goalLogDao.getByClientId(goalId) ?: goalLogDao.getByRemoteId(goalId)
            ?: return Result.failure(IllegalArgumentException("Goal not found: $goalId"))
        if (target.isCompleted) {
            return Result.failure(IllegalStateException("Completed goals cannot be made active"))
        }
        val others = goalLogDao.getActiveGoalsForUser(uid).filter { it.clientId != target.clientId }
        promoteToActive(uid, target, others)
        return Result.success(Unit)
    }

    override suspend fun reorderBacklog(orderedGoalIds: List<String>): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        val now = System.currentTimeMillis()
        val updated = mutableListOf<GoalLogEntity>()
        orderedGoalIds.forEachIndexed { index, id ->
            val entity = goalLogDao.getByClientId(id) ?: goalLogDao.getByRemoteId(id)
            if (entity != null && !entity.isActive && entity.priority != index) {
                updated += entity.copy(
                    priority = index,
                    isSynced = false,
                    updatedAtEpochMillis = now,
                )
            }
        }
        if (updated.isEmpty()) return Result.success(Unit)
        goalLogDao.upsertAll(updated)
        pushUpdatesToFirestore(uid, updated)
        return Result.success(Unit)
    }

    private suspend fun promoteToActive(
        uid: String,
        target: GoalLogEntity,
        others: List<GoalLogEntity>,
    ) {
        val now = System.currentTimeMillis()
        val demoted = others.mapIndexed { index, entity ->
            entity.copy(
                isActive = false,
                priority = index,
                isSynced = false,
                updatedAtEpochMillis = now,
            )
        }
        val promoted = target.copy(
            isActive = true,
            priority = 0,
            isSynced = false,
            updatedAtEpochMillis = now,
        )
        val batch = demoted + promoted
        goalLogDao.upsertAll(batch)
        pushUpdatesToFirestore(uid, batch)
    }

    private suspend fun pushUpdatesToFirestore(uid: String, entities: List<GoalLogEntity>) {
        if (!isOnline()) {
            syncScheduler.enqueue()
            return
        }
        entities.forEach { entity ->
            val remoteId = entity.remoteId ?: entity.clientId
            firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .collection(GOAL_LOGS_COLLECTION)
                .document(remoteId)
                .set(entity.toFirestoreMap(), SetOptions.merge())
                .awaitResult()
        }
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
