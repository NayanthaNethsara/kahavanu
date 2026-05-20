package com.kahavanu.data.expenses.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseLogDao {
    @Query(
        "SELECT * FROM expense_logs WHERE userId = :userId ORDER BY spentAtEpochMillis DESC"
    )
    fun observeLogs(userId: String): Flow<List<ExpenseLogEntity>>

    @Query(
        "SELECT * FROM expense_logs WHERE userId = :userId AND isSynced = 0 ORDER BY createdAtEpochMillis ASC"
    )
    suspend fun getUnsynced(userId: String): List<ExpenseLogEntity>

    @Query("SELECT * FROM expense_logs WHERE userId = :userId")
    suspend fun getLogsForUser(userId: String): List<ExpenseLogEntity>

    @Query("SELECT * FROM expense_logs WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): ExpenseLogEntity?

    @Query("SELECT * FROM expense_logs WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): ExpenseLogEntity?

    @Insert
    suspend fun insert(entry: ExpenseLogEntity): Long

    @Upsert
    suspend fun upsert(entity: ExpenseLogEntity): Long

    @Query("UPDATE expense_logs SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)

    @Query("DELETE FROM expense_logs WHERE localId IN (:localIds)")
    suspend fun deleteByLocalIds(localIds: List<Long>)

    @Query("DELETE FROM expense_logs WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: String)
}
