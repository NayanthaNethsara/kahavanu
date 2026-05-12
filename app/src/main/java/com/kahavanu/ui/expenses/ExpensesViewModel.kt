package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.theme.RawColors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val pendingMatches = listOf(
        PendingExpenseMatch(
            id = "sms-1",
            title = "Keells Super",
            amount = 4_250.0,
            category = "Food",
            confidencePercent = 92,
            receivedAtLabel = "Keells Super · Today, 14:30",
        ),
        PendingExpenseMatch(
            id = "sms-2",
            title = "Uber",
            amount = 850.0,
            category = "Transport",
            confidencePercent = 98,
            receivedAtLabel = "Uber · Today, 09:15",
        ),
    )

    private val selectedPeriod = MutableStateFlow(ExpensePeriod.Month)

    val uiState: StateFlow<ExpensesUiState> = combine(
        selectedPeriod,
        settingsRepository.observeCurrencySettings(),
        expensesRepository.observeExpenseLogs(),
    ) { period, (primaryCurrency, _), allExpenses ->
        val filtered = allExpenses.filter { isWithinPeriod(it.spentAtEpochMillis, period) }
        val categorySummaries = buildCategorySummaries(filtered)

        ExpensesUiState(
            selectedPeriod = period,
            currency = primaryCurrency,
            totalSpent = filtered.sumOf { it.amount },
            budgetLimit = budgetFor(period),
            allExpensesCount = filtered.size,
            categorySummaries = categorySummaries,
            pendingMatches = pendingMatches,
            recentExpenses = filtered
                .sortedByDescending { it.spentAtEpochMillis }
                .take(3)
                .map {
                    RecentExpense(
                        title = it.title,
                        merchant = it.merchant,
                        spentAtEpochMillis = it.spentAtEpochMillis,
                        amount = it.amount,
                        category = normalizeCategory(it.category),
                    )
                },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpensesUiState(
            pendingMatches = pendingMatches,
            budgetLimit = budgetFor(ExpensePeriod.Month),
            categorySummaries = defaultCategorySummaries(),
        ),
    )

    fun onPeriodChange(period: ExpensePeriod) {
        selectedPeriod.update { period }
    }

    private fun buildCategorySummaries(entries: List<ExpenseLogEntry>): List<ExpenseCategorySummary> {
        val byCategory = entries
            .groupBy { normalizeCategory(it.category) }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        return defaultCategorySummaries().map { default ->
            default.copy(amount = byCategory[default.label] ?: 0.0)
        }
    }

    private fun defaultCategorySummaries(): List<ExpenseCategorySummary> {
        return listOf(
            ExpenseCategorySummary("Food", 0.0, RawColors.Amber.Amber600),
            ExpenseCategorySummary("Transport", 0.0, RawColors.Blue.Blue500),
            ExpenseCategorySummary("Utilities", 0.0, RawColors.Violet.Violet500),
            ExpenseCategorySummary("Shopping", 0.0, RawColors.Rose.Rose500),
            ExpenseCategorySummary("Health", 0.0, RawColors.Red.Red500),
            ExpenseCategorySummary("Fun", 0.0, RawColors.Emerald.Emerald500),
        )
    }

    private fun normalizeCategory(rawCategory: String): String {
        return when (rawCategory.trim().lowercase()) {
            "food", "essentials" -> "Food"
            "transport" -> "Transport"
            "utilities" -> "Utilities"
            "shopping", "lifestyle" -> "Shopping"
            "health" -> "Health"
            "fun", "subscriptions" -> "Fun"
            else -> "Food"
        }
    }

    private fun budgetFor(period: ExpensePeriod): Double {
        return when (period) {
            ExpensePeriod.Week -> 55_000.0
            ExpensePeriod.Month -> 180_000.0
            ExpensePeriod.Year -> 2_400_000.0
        }
    }

    private fun isWithinPeriod(epochMillis: Long, period: ExpensePeriod): Boolean {
        val date = java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        return when (period) {
            ExpensePeriod.Week -> !date.isBefore(today.minusDays(6))
            ExpensePeriod.Month -> date.year == today.year && date.month == today.month
            ExpensePeriod.Year -> date.year == today.year
        }
    }

}
