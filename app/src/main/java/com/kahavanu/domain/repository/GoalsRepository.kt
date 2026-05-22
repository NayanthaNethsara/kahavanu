package com.kahavanu.domain.repository

import com.kahavanu.domain.model.GoalAdjustmentLog
import com.kahavanu.domain.model.GoalEntry
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun observeGoals(): Flow<List<GoalEntry>>
    fun observeAdjustmentLogs(goalClientId: String): Flow<List<GoalAdjustmentLog>>
    suspend fun addGoal(goal: GoalEntry): Result<Unit>
    suspend fun updateGoal(goal: GoalEntry): Result<Unit>
    suspend fun adjustSavedAmount(goalId: String, delta: Double): Result<Unit>
    suspend fun deleteGoal(id: String): Result<Unit>
}
