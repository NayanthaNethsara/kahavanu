package com.kahavanu.ui.goals

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import java.time.LocalDate

data class ManageGoalsUiState(
    val activeGoal: GoalEntry? = null,
    val backlogGoals: List<GoalEntry> = emptyList(),
    val completedGoals: List<GoalEntry> = emptyList(),
    val primaryCurrency: CurrencyOption = CurrencyOption.LKR,
    val availableCurrencies: List<CurrencyOption> = CurrencyOption.entries,
    val editingGoalId: String? = null,
    val isSheetOpen: Boolean = false,
    val titleInput: String = "",
    val targetAmountInput: String = "",
    val currentAmountInput: String = "",
    val currencyInput: CurrencyOption = CurrencyOption.LKR,
    val categoryInput: GoalCategory = GoalCategory.SAVINGS,
    val targetDateInput: LocalDate? = null,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
