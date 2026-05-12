package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    expensesRepository: ExpensesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ExpenseHistoryUiState> = combine(
        searchQuery,
        settingsRepository.observeCurrencySettings(),
        expensesRepository.observeExpenseLogs(),
    ) { query, (primaryCurrency, _), allExpenses ->
        val filteredExpenses = if (query.isBlank()) {
            allExpenses
        } else {
            val normalizedQuery = query.trim().lowercase()
            allExpenses.filter { entry ->
                entry.title.lowercase().contains(normalizedQuery) ||
                    entry.category.lowercase().contains(normalizedQuery) ||
                    (entry.merchant?.lowercase()?.contains(normalizedQuery) == true)
            }
        }

        ExpenseHistoryUiState(
            query = query,
            currencyCode = primaryCurrency.code,
            expenses = filteredExpenses.sortedByDescending { it.spentAtEpochMillis },
            totalExpenses = filteredExpenses.sumOf { it.amount },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryUiState(),
    )

    fun onQueryChange(query: String) {
        searchQuery.value = query
    }
}

data class ExpenseHistoryUiState(
    val query: String = "",
    val currencyCode: String = "LKR",
    val expenses: List<ExpenseLogEntry> = emptyList(),
    val totalExpenses: Double = 0.0,
) {
    val transactionCount: Int get() = expenses.size
}
