package com.kahavanu.data.goals.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_adjustment_logs",
    indices = [Index("goalClientId")],
)
data class GoalAdjustmentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val goalClientId: String,
    val userId: String,
    val delta: Double,
    val newAmount: Double,
    val timestampEpochMillis: Long = System.currentTimeMillis(),
)
