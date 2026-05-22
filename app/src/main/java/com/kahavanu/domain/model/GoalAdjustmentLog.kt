package com.kahavanu.domain.model

data class GoalAdjustmentLog(
    val goalId: String,
    val delta: Double,
    val newAmount: Double,
    val timestampEpochMillis: Long,
)
