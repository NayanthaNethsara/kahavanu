package com.kahavanu.data.income.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.kahavanu.data.income.local.IncomeDatabase
import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.data.sync.FirebaseSyncManager
import com.kahavanu.data.sync.SyncState
import com.kahavanu.data.sync.awaitResultVoid
import com.kahavanu.data.sync.awaitResultDocRef
import com.kahavanu.data.sync.awaitResultQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Income-specific sync manager.
 * Handles bidirectional sync for income logs, sources, and scheduled incomes.
 * Firebase is the source of truth.
 */
@Singleton
class IncomeSyncManager @Inject constructor(
    @ApplicationContext context: Context,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    private val database: IncomeDatabase,
) : FirebaseSyncManager(context, firestore, auth) {
    
    override suspend fun sync(): Result<Unit> {
        _syncState.value = SyncState.Syncing
        
        val uid = getCurrentUserId()
            ?: return Result.failure<Unit>(Exception("Not authenticated")).also {
                _syncState.value = SyncState.Error(Exception("Not authenticated"))
            }
        
        return try {
            // 1. Push pending local changes
            pushPendingIncomeLogs(uid)
            pushPendingIncomeSources(uid)
            pushPendingScheduledIncomes(uid)
            
            // 2. Pull latest from Firebase (delta sync)
            val lastSync = getLastSyncTimestamp()
            pullIncomeLogs(uid, lastSync)
            pullIncomeSources(uid, lastSync)
            pullScheduledIncomes(uid, lastSync)
            
            updateLastSyncTimestamp()
            _syncState.value = SyncState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            Result.failure(e)
        }
    }
    
    private suspend fun pushPendingIncomeLogs(uid: String) {
        val dao = database.incomeLogDao()
        val pending = dao.getUnsynced(uid)
        
        for (log in pending) {
            try {
                val data = log.toFirestoreMap()
                val ref = if (log.remoteId != null) {
                    firestore
                        .collection(USERS_COLLECTION)
                        .document(uid)
                        .collection(INCOME_LOGS_COLLECTION)
                        .document(log.remoteId!!)
                        .set(data)
                        .awaitResultVoid()
                        .getOrThrow()
                    log.remoteId!!
                } else {
                    firestore
                        .collection(USERS_COLLECTION)
                        .document(uid)
                        .collection(INCOME_LOGS_COLLECTION)
                        .add(data)
                        .awaitResultDocRef()
                        .getOrThrow().id
                }
                dao.markSynced(log.localId, ref)
            } catch (e: Exception) {
                continue
            }
        }
    }
    
    private suspend fun pushPendingIncomeSources(uid: String) {
        val dao = database.incomeSourceDao()
        val pending = dao.getUnsynced(uid)
        
        for (source in pending) {
            try {
                if (source.isDeleted) {
                    if (source.remoteId != null) {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(INCOME_SOURCES_COLLECTION)
                            .document(source.remoteId!!)
                            .delete()
                            .awaitResultVoid()
                            .getOrThrow()
                        dao.markSynced(source.localId, source.remoteId)
                    }
                } else {
                    val data = source.toFirestoreMap()
                    val ref = if (source.remoteId != null) {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(INCOME_SOURCES_COLLECTION)
                            .document(source.remoteId!!)
                            .set(data)
                            .awaitResultVoid()
                            .getOrThrow()
                        source.remoteId!!
                    } else {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(INCOME_SOURCES_COLLECTION)
                            .add(data)
                            .awaitResultDocRef()
                            .getOrThrow().id
                    }
                    dao.markSynced(source.localId, ref)
                }
            } catch (e: Exception) {
                continue
            }
        }
    }
    
    private suspend fun pushPendingScheduledIncomes(uid: String) {
        val dao = database.scheduledIncomeDao()
        val pending = dao.getUnsynced(uid)
        
        for (scheduled in pending) {
            try {
                if (scheduled.isDeleted) {
                    if (scheduled.remoteId != null) {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(SCHEDULED_COLLECTION)
                            .document(scheduled.remoteId!!)
                            .delete()
                            .awaitResultVoid()
                            .getOrThrow()
                        dao.markSynced(scheduled.localId, scheduled.remoteId)
                    }
                } else {
                    val data = scheduled.toFirestoreMap()
                    val ref = if (scheduled.remoteId != null) {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(SCHEDULED_COLLECTION)
                            .document(scheduled.remoteId!!)
                            .set(data)
                            .awaitResultVoid()
                            .getOrThrow()
                        scheduled.remoteId!!
                    } else {
                        firestore
                            .collection(USERS_COLLECTION)
                            .document(uid)
                            .collection(SCHEDULED_COLLECTION)
                            .add(data)
                            .awaitResultDocRef()
                            .getOrThrow().id
                    }
                    dao.markSynced(scheduled.localId, ref)
                }
            } catch (e: Exception) {
                continue
            }
        }
    }
    
    private suspend fun pullIncomeLogs(uid: String, lastSyncMs: Long) {
        val dao = database.incomeLogDao()
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .get()
              .awaitResultQuery()
        
        if (remoteResult.isFailure) return
        
        val snapshot = remoteResult.getOrThrow()
        for (doc in snapshot.documents) {
            val remoteId = doc.id
            val timestamp = doc.getLong("createdAt") ?: continue
            
            if (timestamp <= lastSyncMs) continue
            
            val remote = doc.toIncomeLogEntity(uid, remoteId)
            val local = dao.getByRemoteId(remoteId)
            
            if (local == null || remote.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                dao.upsert(remote)
            }
        }
    }
    
    private suspend fun pullIncomeSources(uid: String, lastSyncMs: Long) {
        val dao = database.incomeSourceDao()
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .get()
              .awaitResultQuery()
        
        if (remoteResult.isFailure) return
        
        val snapshot = remoteResult.getOrThrow()
        val remoteIds = mutableSetOf<String>()
        
        for (doc in snapshot.documents) {
            val remoteId = doc.id
            remoteIds.add(remoteId)
            
            val timestamp = doc.getLong("createdAt") ?: continue
            if (timestamp <= lastSyncMs) continue
            
            val remote = doc.toIncomeSourceEntity(uid, remoteId)
            val local = dao.getByRemoteId(remoteId)
            
            if (local == null || remote.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                dao.upsert(remote)
            }
        }
        
        val syncedRemoteIds = dao.getSyncedRemoteIds(uid)
        val missingRemoteIds = syncedRemoteIds.filter { it !in remoteIds }
        if (missingRemoteIds.isNotEmpty()) {
            dao.markDeletedByRemoteIds(uid, missingRemoteIds)
        }
    }
    
    private suspend fun pullScheduledIncomes(uid: String, lastSyncMs: Long) {
        val dao = database.scheduledIncomeDao()
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .get()
              .awaitResultQuery()
        
        if (remoteResult.isFailure) return
        
        val snapshot = remoteResult.getOrThrow()
        val remoteIds = mutableSetOf<String>()
        
        for (doc in snapshot.documents) {
            val remoteId = doc.id
            remoteIds.add(remoteId)
            
            val timestamp = doc.getLong("createdAt") ?: continue
            if (timestamp <= lastSyncMs) continue
            
            val remote = doc.toScheduledIncomeEntity(uid, remoteId)
            val local = dao.getByRemoteId(remoteId)
            
            if (local == null || remote.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                dao.upsert(remote)
            }
        }
        
        val syncedRemoteIds = dao.getSyncedRemoteIds(uid)
        val missingRemoteIds = syncedRemoteIds.filter { it !in remoteIds }
        if (missingRemoteIds.isNotEmpty()) {
            dao.markDeletedByRemoteIds(uid, missingRemoteIds)
        }
    }
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val INCOME_LOGS_COLLECTION = "incomeLogs"
        private const val INCOME_SOURCES_COLLECTION = "incomeSources"
        private const val SCHEDULED_COLLECTION = "scheduledIncomes"
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toIncomeLogEntity(uid: String, remoteId: String): IncomeLogEntity {
    return IncomeLogEntity(
        userId = uid,
        title = getString("title") ?: "",
        amount = getDouble("amount") ?: 0.0,
        currency = getString("currency") ?: "",
        receivedAtEpochMillis = getLong("receivedAt") ?: System.currentTimeMillis(),
        createdAtEpochMillis = getLong("createdAt") ?: System.currentTimeMillis(),
        sourceId = getLong("sourceId"),
        sourceName = getString("sourceName"),
        sourceType = getString("sourceType"),
        isInvoiceSent = getBoolean("isInvoiceSent") ?: false,
        frequency = getString("frequency"),
        contactName = getString("contactName"),
        contactNumber = getString("contactNumber"),
        remoteId = remoteId,
        isSynced = true,
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toIncomeSourceEntity(uid: String, remoteId: String): IncomeSourceEntity {
    val typesList = (get("types") as? List<*>)?.mapNotNull { it as? String }?.filter { it.isNotBlank() } ?: emptyList()
    return IncomeSourceEntity(
        userId = uid,
        name = getString("name") ?: "",
        typesCsv = typesList.joinToString(","),
        createdAtEpochMillis = getLong("createdAt") ?: System.currentTimeMillis(),
        updatedAtEpochMillis = getLong("updatedAt") ?: System.currentTimeMillis(),
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toScheduledIncomeEntity(uid: String, remoteId: String): ScheduledIncomeEntity {
    return ScheduledIncomeEntity(
        userId = uid,
        title = getString("title") ?: "",
        amount = getDouble("amount") ?: 0.0,
        currency = getString("currency") ?: "",
        type = getString("type") ?: "pending",
        frequency = getString("frequency"),
        scheduledDateEpochMillis = getLong("scheduledDate") ?: System.currentTimeMillis(),
        lastGeneratedEpochMillis = getLong("lastGenerated") ?: getLong("receivedAt"),
        sourceId = getLong("sourceId"),
        sourceName = getString("sourceName"),
        isInvoiceSent = getBoolean("isInvoiceSent") ?: false,
        contactName = getString("contactName"),
        contactNumber = getString("contactNumber"),
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
    )
}
