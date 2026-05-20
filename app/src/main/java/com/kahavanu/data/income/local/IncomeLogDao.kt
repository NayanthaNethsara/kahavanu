package com.kahavanu.data.income.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeLogDao {
    @Query(
        "SELECT * FROM income_logs WHERE userId = :userId ORDER BY receivedAtEpochMillis DESC"
    )
    fun observeLogs(userId: String): Flow<List<IncomeLogEntity>>

    @Query(
        "SELECT * FROM income_logs WHERE userId = :userId AND isSynced = 0 ORDER BY createdAtEpochMillis ASC"
    )
    suspend fun getUnsynced(userId: String): List<IncomeLogEntity>

    @Query("SELECT * FROM income_logs WHERE userId = :userId")
    suspend fun getLogsForUser(userId: String): List<IncomeLogEntity>

    @Query("SELECT * FROM income_logs WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): IncomeLogEntity?

    @Query("SELECT * FROM income_logs WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): IncomeLogEntity?

    @Insert
    suspend fun insert(entry: IncomeLogEntity): Long

    @Upsert
    suspend fun upsert(entity: IncomeLogEntity): Long

    @Query("UPDATE income_logs SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)

    @Query("DELETE FROM income_logs WHERE localId IN (:localIds)")
    suspend fun deleteByLocalIds(localIds: List<Long>)

    @Query("DELETE FROM income_logs WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: String)
}
