package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.expenses.components.ExpensesActionButtons
import com.kahavanu.ui.expenses.components.ExpensesHeader
import com.kahavanu.ui.expenses.components.ExpensesSummaryCard
import com.kahavanu.ui.expenses.components.RecentExpensesSection
import com.kahavanu.ui.expenses.components.SpendingInsightsSection
import com.kahavanu.ui.expenses.components.UpcomingBillsSection
import com.kahavanu.ui.expenses.components.currentMonthLabel
import com.kahavanu.ui.theme.RawColors
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Rose.Rose50.copy(alpha = 0.5f),
                        RawColors.Slate.Slate100,
                    ),
                ),
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 140.dp,
            bottom = 140.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge),
    ) {
        item { ExpensesHeader() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                ExpensesSummaryCard(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodChange = viewModel::onPeriodChange,
                    currency = uiState.currency,
                    totalSpent = uiState.totalSpent,
                    budgetLimit = uiState.budgetLimit,
                    categorySummaries = uiState.categorySummaries,
                    monthLabel = monthLabel,
                )
                ExpensesActionButtons(
                    onLogExpense = onLogExpense,
                    onViewBills = onViewBills,
                    onViewBudgets = onViewBudgets,
                    onViewHistory = onViewHistory,
                )
            }
        }
        item {
            SpendingInsightsSection(
                selectedPeriod = uiState.selectedPeriod,
                currency = uiState.currency,
                totalSpent = uiState.totalSpent,
                categories = uiState.categorySummaries,
                recentCount = uiState.recentExpenses.size,
                upcomingCount = uiState.upcomingBills.size,
            )
        }
        item {
            UpcomingBillsSection(
                bills = uiState.upcomingBills,
                currency = uiState.currency,
                onViewAll = onViewBills,
            )
        }
        item {
            RecentExpensesSection(
                expenses = uiState.recentExpenses,
                currency = uiState.currency,
                onViewAll = onViewHistory,
            )
        }
    }
}
