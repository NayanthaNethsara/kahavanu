package com.kahavanu.ui.goals

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import java.time.LocalDate

enum class GoalSortMode(val label: String) {
    BY_PRIORITY("Priority"),
    BY_PROGRESS("Progress"),
    BY_REMAINING("Remaining"),
    BY_DATE("Target Date"),
}

data class CapacityBreakdown(
    val monthlyIncome: Double = 0.0,
    val committed: Double = 0.0,
    val discretionary: Double = 0.0,
    val realCapacityToSave: Double = 0.0,
)

data class GoalsUiState(
    val activeGoal: GoalEntry? = null,
    val backlogGoals: List<GoalEntry> = emptyList(),
    val completedGoals: List<GoalEntry> = emptyList(),
    val totalTargetAmount: Double = 0.0,
    val totalSavedAmount: Double = 0.0,
    val currency: CurrencyOption = CurrencyOption.LKR,
    val activeGoalSoftLimit: Int = 5,
    val isAtActiveGoalLimit: Boolean = false,
    val sortMode: GoalSortMode = GoalSortMode.BY_PRIORITY,
    val capacity: CapacityBreakdown = CapacityBreakdown(),
)

data class GoalSetupUiState(
    val title: String = "",
    val targetAmount: String = "",
    val currentAmount: String = "",
    val currency: CurrencyOption = CurrencyOption.LKR,
    val category: GoalCategory = GoalCategory.SAVINGS,
    val targetDate: LocalDate? = null,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val availableCurrencies: List<CurrencyOption> = CurrencyOption.entries,
)
