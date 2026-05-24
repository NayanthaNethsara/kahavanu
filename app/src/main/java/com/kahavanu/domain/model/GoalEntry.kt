package com.kahavanu.domain.model

enum class GoalCategory(val label: String) {
    SAVINGS("Savings"),
    TRAVEL("Travel"),
    EMERGENCY("Emergency Fund"),
    EDUCATION("Education"),
    PURCHASE("Purchase"),
    INVESTMENT("Investment"),
    OTHER("Other"),
}

data class GoalEntry(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: CurrencyOption,
    val category: GoalCategory,
    val targetDateEpochMillis: Long?,
    val isCompleted: Boolean,
    val createdAtEpochMillis: Long,
    val lastUpdatedEpochMillis: Long = createdAtEpochMillis,
    val isActive: Boolean = false,
    val priority: Int = 0,
)
