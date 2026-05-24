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

    // Promotes the given goal to be the single active/featured goal. All other goals
    // for the user are demoted to backlog.
    suspend fun setActiveGoal(goalId: String): Result<Unit>

    // Persists a new backlog ordering. The list contains the ids of backlog goals in
    // the order they should appear (priority 0 = first).
    suspend fun reorderBacklog(orderedGoalIds: List<String>): Result<Unit>
}
