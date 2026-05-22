package com.kahavanu.ui.expenses

import com.kahavanu.ui.util.currentMonthLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.History
import com.kahavanu.ui.common.KahavanuScreen
import com.kahavanu.ui.common.QuickAction
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.screenSection
import com.kahavanu.ui.expenses.components.ByCategorySection
import com.kahavanu.ui.expenses.components.ExpensesSummaryCard
import com.kahavanu.ui.expenses.components.MatchAndCategorizeSection
import com.kahavanu.ui.expenses.components.RecentExpensesSection
import com.kahavanu.ui.theme.Spacing

@Composable
fun ExpensesScreen(
    onLogExpense: () -> Unit = {},
    onViewBills: () -> Unit = {},
    onViewBudgets: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    viewModel: ExpensesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val monthLabel = currentMonthLabel()

    KahavanuScreen(
        headerLabel = "Expenses",
        headerTitle = "Where Money Goes",
    ) {
        screenSection {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                ExpensesSummaryCard(
                    selectedPeriod = uiState.selectedPeriod,
                    currency = uiState.currency,
                    totalSpent = uiState.totalSpent,
                    budgetLimit = uiState.budgetLimit,
                    categorySummaries = uiState.categorySummaries,
                    monthLabel = monthLabel,
                )
                QuickActionRow(
                    actions = listOf(
                        QuickAction(Icons.Outlined.Add, "Log Expense", onLogExpense),
                        QuickAction(Icons.Outlined.BarChart, "Budgets", onViewBudgets),
                        QuickAction(Icons.AutoMirrored.Outlined.ReceiptLong, "Bills", onViewBills),
                        QuickAction(Icons.Outlined.History, "History", onViewHistory),
                    ),
                )
            }
        }
        screenSection {
            MatchAndCategorizeSection(
                items = uiState.pendingMatches,
                currency = uiState.currency,
                onConfirm = viewModel::confirmExpenseSuggestion,
                onDismiss = viewModel::dismissExpenseSuggestion,
            )
        }
        screenSection {
            ByCategorySection(
                categories = uiState.categorySummaries,
                currency = uiState.currency,
            )
        }
        screenSection {
            RecentExpensesSection(
                expenses = uiState.recentExpenses,
                currency = uiState.currency,
                onViewAll = onViewHistory,
            )
        }
    }
}
