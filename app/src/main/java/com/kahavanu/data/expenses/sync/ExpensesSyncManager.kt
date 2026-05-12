package com.kahavanu.data.expenses.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.expenses.local.ExpenseLogEntity
import com.kahavanu.data.sync.FirebaseSyncManager
import com.kahavanu.data.sync.SyncState
import com.kahavanu.data.sync.awaitResultVoid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Expense-specific sync manager.
 * Handles bidirectional sync for expense logs.
 * Firebase is the source of truth.
 */
@Singleton
class ExpensesSyncManager @Inject constructor(
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
            // 1. Push pending local changes
            pushPendingExpenseLogs(uid)

            // 2. Pull latest from Firebase (delta sync)
            val lastSync = getLastSyncTimestamp()
            pullExpenseLogs(uid, lastSync)

            updateLastSyncTimestamp()
            _syncState.value = SyncState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            Result.failure(e)
        }
    }

    private suspend fun pushPendingExpenseLogs(uid: String) {
        val dao = database.expenseLogDao()
        val pending = dao.getUnsynced(uid)
        var hadFailure = false
        for (log in pending) {
            try {
                val data = log.toFirestoreMap()
                val docId = log.remoteId ?: log.clientId
                firestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .collection(EXPENSE_LOGS_COLLECTION)
                    .document(docId)
                    .set(data)
                    .awaitResultVoid()
                    .getOrThrow()
                dao.markSynced(log.localId, docId)
            } catch (e: Exception) {
                android.util.Log.w(
                    "ExpensesSyncManager",
                    "Failed to push expense log localId=${log.localId}",
                    e
                )
                hadFailure = true
            }
        }
        if (hadFailure) throw Exception("Failed to push some expense logs")
    }

    private suspend fun pullExpenseLogs(uid: String, lastSyncTimestamp: Long) {
        val dao = database.expenseLogDao()
        try {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .collection(EXPENSE_LOGS_COLLECTION)
                .whereGreaterThanOrEqualTo("updatedAtEpochMillis", lastSyncTimestamp)
                .get()
                .await()

            val entities = snapshot.documents.mapNotNull { doc ->
                try {
                    val clientId = doc.id
                    val remoteId = doc.id
                    val userId = uid

                    // Parse fields from Firestore document
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val currency = doc.getString("currency") ?: "USD"
                    val spentAtEpochMillis = doc.getLong("spentAtEpochMillis") ?: 0L
                    val createdAtEpochMillis = doc.getLong("createdAtEpochMillis")
                        ?: System.currentTimeMillis()
                    val merchant = doc.getString("merchant")
                    val category = doc.getString("category") ?: "Other"
                    val notes = doc.getString("notes")
                    val paymentMethod = doc.getString("paymentMethod") ?: "Cash"
                    val updatedAtEpochMillis = doc.getLong("updatedAtEpochMillis")
                        ?: System.currentTimeMillis()

                    ExpenseLogEntity(
                        localId = 0,
                        userId = userId,
                        title = title,
                        amount = amount,
                        currency = currency,
                        spentAtEpochMillis = spentAtEpochMillis,
                        createdAtEpochMillis = createdAtEpochMillis,
                        merchant = merchant,
                        category = category,
                        notes = notes,
                        paymentMethod = paymentMethod,
                        clientId = clientId,
                        remoteId = remoteId,
                        isSynced = true,
                        isDeleted = false,
                        updatedAtEpochMillis = updatedAtEpochMillis
                    )
                } catch (e: Exception) {
                    android.util.Log.w("ExpensesSyncManager", "Failed to parse expense log doc=${doc.id}", e)
                    null
                }
            }

            // Upsert entities into local DB
            for (entity in entities) {
                val existing = dao.getByRemoteId(entity.remoteId!!)
                if (existing != null) {
                    // Update existing by remoteId
                    dao.upsert(entity.copy(localId = existing.localId))
                } else {
                    // Check for duplicate by clientId
                    val existingByClientId = dao.getByClientId(entity.clientId)
                    if (existingByClientId != null) {
                        dao.upsert(entity.copy(localId = existingByClientId.localId))
                    } else {
                        dao.insert(entity)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ExpensesSyncManager", "Failed to pull expense logs", e)
            throw e
        }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val EXPENSE_LOGS_COLLECTION = "expenseLogs"
    }
}
