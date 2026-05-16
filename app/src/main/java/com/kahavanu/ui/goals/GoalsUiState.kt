package com.kahavanu.ui.goals

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import java.time.LocalDate

data class GoalsUiState(
    val activeGoals: List<GoalEntry> = emptyList(),
    val completedGoals: List<GoalEntry> = emptyList(),
    val totalTargetAmount: Double = 0.0,
    val totalSavedAmount: Double = 0.0,
    val currency: CurrencyOption = CurrencyOption.LKR,
    val activeGoalSoftLimit: Int = 5,
    val isAtActiveGoalLimit: Boolean = false,
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
