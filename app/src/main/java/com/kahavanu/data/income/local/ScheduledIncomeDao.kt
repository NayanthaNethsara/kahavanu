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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScheduledIncomeEntity): Long

    @Query("UPDATE scheduled_income SET isDeleted = 1 WHERE localId = :localId")
    suspend fun markDeleted(localId: Long)

    @Query("SELECT * FROM scheduled_income WHERE localId = :localId LIMIT 1")
    suspend fun getById(localId: Long): ScheduledIncomeEntity?
}
