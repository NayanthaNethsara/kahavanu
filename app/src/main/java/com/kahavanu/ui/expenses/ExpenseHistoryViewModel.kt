package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.common.HistoryDateRange
import com.kahavanu.ui.common.contains
import com.kahavanu.ui.util.CurrencyConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

const val EXPENSE_FILTER_ALL = "All"

data class ExpenseFilters(
    val category: String = EXPENSE_FILTER_ALL,
    val dateRange: HistoryDateRange = HistoryDateRange.ALL,
    val paymentMethod: String = EXPENSE_FILTER_ALL,
    val minAmount: String = "",
    val maxAmount: String = "",
) {
    val isActive: Boolean
        get() = category != EXPENSE_FILTER_ALL ||
            dateRange != HistoryDateRange.ALL ||
            paymentMethod != EXPENSE_FILTER_ALL ||
            minAmount.isNotBlank() ||
            maxAmount.isNotBlank()
}

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    expensesRepository: ExpensesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filters = MutableStateFlow(ExpenseFilters())

    val uiState: StateFlow<ExpenseHistoryUiState> = combine(
        searchQuery,
        filters,
        settingsRepository.observeCurrencySettings(),
        expensesRepository.observeExpenseLogs(),
    ) { query, activeFilters, currency, allExpenses ->
        val (primaryCurrency, _) = currency

        val availableCategories = (listOf(EXPENSE_FILTER_ALL) +
            allExpenses.map { it.category }.distinct().sorted())
        val availablePaymentMethods = (listOf(EXPENSE_FILTER_ALL) +
            allExpenses.mapNotNull { it.paymentMethod?.takeIf(String::isNotBlank) }.distinct().sorted())

        val minAmt = activeFilters.minAmount.toDoubleOrNull()
        val maxAmt = activeFilters.maxAmount.toDoubleOrNull()

        val filteredExpenses = allExpenses.filter { entry ->
            val matchesCategory = activeFilters.category == EXPENSE_FILTER_ALL ||
                entry.category.equals(activeFilters.category, ignoreCase = true)
            val matchesPayment = activeFilters.paymentMethod == EXPENSE_FILTER_ALL ||
                entry.paymentMethod?.equals(activeFilters.paymentMethod, ignoreCase = true) == true
            val matchesDate = activeFilters.dateRange.contains(entry.spentAtEpochMillis)
            val matchesMin = minAmt == null || entry.amount >= minAmt
            val matchesMax = maxAmt == null || entry.amount <= maxAmt
            val matchesSearch = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                entry.title.lowercase().contains(q) ||
                    entry.category.lowercase().contains(q) ||
                    (entry.merchant?.lowercase()?.contains(q) == true)
            }
            matchesCategory && matchesPayment && matchesDate && matchesMin && matchesMax && matchesSearch
        }

        ExpenseHistoryUiState(
            query = query,
            filters = activeFilters,
            availableCategories = availableCategories,
            availablePaymentMethods = availablePaymentMethods,
            currencyCode = primaryCurrency.code,
            expenses = filteredExpenses.sortedByDescending { it.spentAtEpochMillis },
            // Total folds every currency into the primary one (static rates); list items keep their own.
            totalExpenses = filteredExpenses
                .sumOf { CurrencyConverter.convert(it.amount, it.currency, primaryCurrency.code) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryUiState(),
    )

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFiltersChange(update: (ExpenseFilters) -> ExpenseFilters) {
        filters.value = update(filters.value)
    }

    fun clearFilters() {
        filters.value = ExpenseFilters()
    }
}

data class ExpenseHistoryUiState(
    val query: String = "",
    val filters: ExpenseFilters = ExpenseFilters(),
    val availableCategories: List<String> = listOf(EXPENSE_FILTER_ALL),
    val availablePaymentMethods: List<String> = listOf(EXPENSE_FILTER_ALL),
    val currencyCode: String = "LKR",
    val expenses: List<ExpenseLogEntry> = emptyList(),
    val totalExpenses: Double = 0.0,
) {
    val transactionCount: Int get() = expenses.size
    val hasActiveFilter: Boolean get() = filters.isActive
    val selectedCategory: String get() = filters.category
}
