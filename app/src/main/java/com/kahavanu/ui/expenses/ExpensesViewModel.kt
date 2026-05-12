package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.theme.RawColors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val periodSummaries = mapOf(
        ExpensePeriod.Week to PeriodSummary(
            totalSpent = 42_600.0,
            budgetLimit = 55_000.0,
            categorySummaries = listOf(
                ExpenseCategorySummary("Essentials", 21_500.0, RawColors.Rose.Rose500),
                ExpenseCategorySummary("Lifestyle", 12_300.0, RawColors.Amber.Amber500),
                ExpenseCategorySummary("Transport", 5_700.0, RawColors.Blue.Blue500),
                ExpenseCategorySummary("Subscriptions", 3_100.0, RawColors.Indigo.Indigo400),
            ),
        ),
        ExpensePeriod.Month to PeriodSummary(
            totalSpent = 186_450.0,
            budgetLimit = 250_000.0,
            categorySummaries = listOf(
                ExpenseCategorySummary("Essentials", 86_000.0, RawColors.Rose.Rose500),
                ExpenseCategorySummary("Lifestyle", 52_400.0, RawColors.Amber.Amber500),
                ExpenseCategorySummary("Transport", 22_900.0, RawColors.Blue.Blue500),
                ExpenseCategorySummary("Subscriptions", 25_150.0, RawColors.Indigo.Indigo400),
            ),
        ),
        ExpensePeriod.Year to PeriodSummary(
            totalSpent = 1_854_300.0,
            budgetLimit = 2_400_000.0,
            categorySummaries = listOf(
                ExpenseCategorySummary("Essentials", 812_000.0, RawColors.Rose.Rose500),
                ExpenseCategorySummary("Lifestyle", 540_000.0, RawColors.Amber.Amber500),
                ExpenseCategorySummary("Transport", 238_600.0, RawColors.Blue.Blue500),
                ExpenseCategorySummary("Subscriptions", 263_700.0, RawColors.Indigo.Indigo400),
            ),
        ),
    )

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<ExpensesUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.observeCurrencySettings().collect { (primary, _) ->
                _uiState.update { current -> current.copy(currency = primary) }
            }
        }
    }

    fun onPeriodChange(period: ExpensePeriod) {
        val summary = periodSummaries[period] ?: periodSummaries.getValue(ExpensePeriod.Month)
        _uiState.update { current ->
            current.copy(
                selectedPeriod = period,
                totalSpent = summary.totalSpent,
                budgetLimit = summary.budgetLimit,
                categorySummaries = summary.categorySummaries,
            )
        }
    }

    private fun buildInitialState(): ExpensesUiState {
        val monthSummary = periodSummaries.getValue(ExpensePeriod.Month)
        val today = LocalDate.now()
        return ExpensesUiState(
            selectedPeriod = ExpensePeriod.Month,
            totalSpent = monthSummary.totalSpent,
            budgetLimit = monthSummary.budgetLimit,
            categorySummaries = monthSummary.categorySummaries,
            upcomingBills = listOf(
                UpcomingBill("Office rent", 120_000.0, startOfDay(today.plusDays(2))),
                UpcomingBill("Power bill", 18_450.0, startOfDay(today.plusDays(5))),
                UpcomingBill("Spotify family", 1_650.0, startOfDay(today.minusDays(1))),
            ),
            recentExpenses = listOf(
                RecentExpense("Groceries", "Keells", startOfDay(today.minusDays(1)), 12_540.0, "Essentials"),
                RecentExpense("Cab ride", "PickMe", startOfDay(today.minusDays(2)), 3_450.0, "Transport"),
                RecentExpense("Team lunch", "Cafe Noir", startOfDay(today.minusDays(3)), 9_200.0, "Lifestyle"),
                RecentExpense("Cloud storage", "Google", startOfDay(today.minusDays(4)), 1_200.0, "Subscriptions"),
            ),
        )
    }

    private fun startOfDay(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private data class PeriodSummary(
        val totalSpent: Double,
        val budgetLimit: Double,
        val categorySummaries: List<ExpenseCategorySummary>,
    )
}
