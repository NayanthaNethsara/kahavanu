package com.kahavanu.data.income.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledIncomeDao {
    @Query("SELECT * FROM scheduled_income WHERE userId = :userId AND isDeleted = 0")
    fun observeScheduled(userId: String): Flow<List<ScheduledIncomeEntity>>

    @Query("SELECT * FROM scheduled_income WHERE userId = :userId AND isDeleted = 0 AND scheduledDateEpochMillis <= :now")
    suspend fun getDueScheduled(userId: String, now: Long): List<ScheduledIncomeEntity>

    @Query("SELECT * FROM scheduled_income WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsynced(userId: String): List<ScheduledIncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScheduledIncomeEntity): Long

    @Query("UPDATE scheduled_income SET isDeleted = 1, isSynced = 0 WHERE localId = :localId")
    suspend fun markDeleted(localId: Long)

    @Query("SELECT * FROM scheduled_income WHERE localId = :localId LIMIT 1")
    suspend fun getById(localId: Long): ScheduledIncomeEntity?

    @Query("SELECT * FROM scheduled_income WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): ScheduledIncomeEntity?

    @Query("UPDATE scheduled_income SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String?)

    @Query(
        "SELECT remoteId FROM scheduled_income WHERE userId = :userId AND remoteId IS NOT NULL AND isSynced = 1"
    )
    suspend fun getSyncedRemoteIds(userId: String): List<String>

    @Query(
        "UPDATE scheduled_income SET isDeleted = 1, isSynced = 1 WHERE userId = :userId AND remoteId IN (:remoteIds)"
    )
    suspend fun markDeletedByRemoteIds(userId: String, remoteIds: List<String>)
}
