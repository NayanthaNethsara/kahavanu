package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.util.CurrencyConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExpenseGraphViewModel @Inject constructor(
    expensesRepository: ExpensesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val stats: StateFlow<ExpenseStats> = combine(
        expensesRepository.observeExpenseLogs(),
        settingsRepository.observeCurrencySettings(),
    ) { expenses, (primaryCurrency, _) ->
        buildStats(expenses, primaryCurrency.code)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseStats(),
    )

    private fun buildStats(expenses: List<ExpenseLogEntry>, currencyCode: String): ExpenseStats {
        // Fold every currency into the primary one (static rates) so secondary logs aren't dropped.
        fun ExpenseLogEntry.spentInPrimary(): Double =
            CurrencyConverter.convert(amount, currency, currencyCode)
        val totalSpent = expenses.sumOf { it.spentInPrimary() }
        val categoryBreakdown = expenses
            .groupBy { it.category }
            .mapValues { (_, entries) -> entries.sumOf { it.spentInPrimary() } }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

        return ExpenseStats(
            currencyCode = currencyCode,
            totalSpent = totalSpent,
            categoryBreakdown = categoryBreakdown,
        )
    }
}

data class ExpenseStats(
    val currencyCode: String = "LKR",
    val totalSpent: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
)
