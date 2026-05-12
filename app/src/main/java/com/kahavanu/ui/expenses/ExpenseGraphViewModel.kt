package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseGraphViewModel @Inject constructor(
    expensesRepository: ExpensesRepository,
) : ViewModel() {

    private val _stats = MutableStateFlow(ExpenseStats())
    val stats: StateFlow<ExpenseStats> = _stats

    init {
        viewModelScope.launch {
            expensesRepository.observeExpenseLogs()
                .map { expenses ->
                    buildStats(expenses)
                }
                .collect { stats ->
                    _stats.value = stats
                }
        }
    }

    private fun buildStats(expenses: List<ExpenseLogEntry>): ExpenseStats {
        val totalSpent = expenses.sumOf { it.amount }
        val categoryBreakdown = expenses
            .groupBy { it.category }
            .mapValues { (_, entries) -> entries.sumOf { it.amount } }
            .toSortedMap { a, b -> b.compareTo(a) }

        return ExpenseStats(
            totalSpent = totalSpent,
            categoryBreakdown = categoryBreakdown,
        )
    }
}

data class ExpenseStats(
    val totalSpent: Double = 0.0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
)
