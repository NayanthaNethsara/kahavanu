package com.kahavanu.data.goals.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalAdjustmentLogDao {
    @Query("SELECT * FROM goal_adjustment_logs WHERE goalClientId = :goalClientId ORDER BY timestampEpochMillis DESC")
    fun observeLogsForGoal(goalClientId: String): Flow<List<GoalAdjustmentLogEntity>>

    @Insert
    suspend fun insert(log: GoalAdjustmentLogEntity)

    @Query("DELETE FROM goal_adjustment_logs WHERE goalClientId = :goalClientId")
    suspend fun deleteForGoal(goalClientId: String)
}
