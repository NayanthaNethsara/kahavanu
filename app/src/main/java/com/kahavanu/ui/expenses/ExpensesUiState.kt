package com.kahavanu.ui.expenses

import androidx.compose.ui.graphics.Color
import com.kahavanu.domain.model.CurrencyOption

enum class ExpensePeriod(val label: String) {
    Week("Week"),
    Month("Month"),
    Year("Year"),
}

data class ExpenseCategorySummary(
    val label: String,
    val amount: Double,
    val color: Color,
)

data class RecentExpense(
    val title: String,
    val merchant: String?,
    val spentAtEpochMillis: Long,
    val amount: Double,
    val category: String,
)

data class PendingExpenseMatch(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val confidencePercent: Int,
    val receivedAtLabel: String,
)

data class ExpensesUiState(
    val selectedPeriod: ExpensePeriod = ExpensePeriod.Month,
    val currency: CurrencyOption = CurrencyOption.LKR,
    val totalSpent: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val allExpensesCount: Int = 0,
    val categorySummaries: List<ExpenseCategorySummary> = emptyList(),
    val pendingMatches: List<PendingExpenseMatch> = emptyList(),
    val recentExpenses: List<RecentExpense> = emptyList(),
    val subscriptionCost: Double = 0.0,
    val subscriptionCount: Int = 0,
)
