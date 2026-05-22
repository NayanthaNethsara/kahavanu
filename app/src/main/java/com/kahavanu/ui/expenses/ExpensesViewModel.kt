package com.kahavanu.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import com.kahavanu.ui.home.inferExpenseCategory
import com.kahavanu.ui.theme.CategoryFun
import com.kahavanu.ui.theme.CategoryHealth
import com.kahavanu.ui.theme.CategoryShopping
import com.kahavanu.ui.theme.CategoryTransport
import com.kahavanu.ui.theme.CategoryUtilities
import com.kahavanu.ui.theme.Warning
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val settingsRepository: SettingsRepository,
    private val smsSuggestionRepository: SmsSuggestionRepository,
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(ExpensePeriod.Month)

    val pendingExpenseMatches: StateFlow<List<PendingExpenseMatch>> =
        smsSuggestionRepository.observePendingByKinds(listOf(SuggestionKind.EXPENSE))
            .map { suggestions ->
                suggestions.map { s ->
                    PendingExpenseMatch(
                        id = s.localId.toString(),
                        title = s.title,
                        amount = s.amount,
                        category = inferExpenseCategory(s.smsSenderName, s.merchant),
                        confidencePercent = (s.confidence * 100).toInt(),
                        receivedAtLabel = buildLabel(s.merchant ?: s.smsSenderName, s.smsReceivedAtEpochMillis),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val uiState: StateFlow<ExpensesUiState> = combine(
        selectedPeriod,
        settingsRepository.observeCurrencySettings(),
        expensesRepository.observeExpenseLogs(),
        pendingExpenseMatches,
    ) { period, (primaryCurrency, _), allExpenses, pendingMatches ->
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
            budgetLimit = budgetFor(ExpensePeriod.Month),
            categorySummaries = defaultCategorySummaries(),
        ),
    )

    fun onPeriodChange(period: ExpensePeriod) {
        selectedPeriod.update { period }
    }

    fun confirmExpenseSuggestion(id: String) {
        viewModelScope.launch {
            val localId = id.toLongOrNull() ?: return@launch
            val s = smsSuggestionRepository.getById(localId) ?: return@launch
            expensesRepository.logExpense(
                ExpenseLogEntry(
                    title = s.title,
                    amount = s.amount,
                    currency = s.currency,
                    spentAtEpochMillis = s.txnAtEpochMillis,
                    merchant = s.merchant,
                    category = inferExpenseCategory(s.smsSenderName, s.merchant),
                )
            )
            smsSuggestionRepository.confirm(localId)
        }
    }

    fun dismissExpenseSuggestion(id: String) {
        viewModelScope.launch {
            smsSuggestionRepository.dismiss(id.toLongOrNull() ?: return@launch)
        }
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
            ExpenseCategorySummary("Food", 0.0, Warning),
            ExpenseCategorySummary("Transport", 0.0, CategoryTransport),
            ExpenseCategorySummary("Utilities", 0.0, CategoryUtilities),
            ExpenseCategorySummary("Shopping", 0.0, CategoryShopping),
            ExpenseCategorySummary("Health", 0.0, CategoryHealth),
            ExpenseCategorySummary("Fun", 0.0, CategoryFun),
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
            else -> "Shopping"
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

private fun buildLabel(merchant: String, epochMillis: Long): String {
    val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(epochMillis))
    return "$merchant · $dateStr"
}
