package com.kahavanu.data.income.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Query("SELECT * FROM income_logs WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): IncomeLogEntity?

    @Insert
    suspend fun insert(entry: IncomeLogEntity): Long

    @Query("UPDATE income_logs SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)
}
