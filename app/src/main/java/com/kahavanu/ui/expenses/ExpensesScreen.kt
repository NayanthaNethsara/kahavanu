package com.kahavanu.ui.expenses

import com.kahavanu.ui.util.currentMonthLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import com.kahavanu.ui.common.KahavanuScreen
import com.kahavanu.ui.common.QuickAction
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.screenSection
import com.kahavanu.ui.expenses.components.ByCategorySection
import com.kahavanu.ui.expenses.components.ExpensesSummaryCard
import com.kahavanu.ui.expenses.components.ExpensesInsightsSection
import com.kahavanu.ui.expenses.components.MatchAndCategorizeSection
import com.kahavanu.ui.expenses.components.RecentExpensesSection
import com.kahavanu.ui.theme.Spacing

@Composable
fun ExpensesScreen(
    onLogExpense: () -> Unit = {},
    onViewBills: () -> Unit = {},
    onViewSubscriptions: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    viewModel: ExpensesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val monthLabel = currentMonthLabel()
    var showBudgetDialog by remember { mutableStateOf(false) }

    // Dynamically calculate the biggest spend metrics based on database summaries
    val activeSummaries = uiState.categorySummaries.filter { it.amount > 0.0 }
    val biggestSpend = activeSummaries.maxByOrNull { it.amount }
    val totalSpent = uiState.totalSpent

    val biggestSpendCategory = biggestSpend?.label ?: "—"
    val biggestSpendAmount = biggestSpend?.amount ?: 0.0
    val biggestSpendPercent = if (totalSpent > 0.0) ((biggestSpendAmount / totalSpent) * 100).toInt() else 0
    val biggestSpendSubtitle = "LKR ${String.format("%,.0f", biggestSpendAmount / 1000)}K · $biggestSpendPercent%"
    val biggestSpendSubtitleText = "LKR ${String.format("%,.0f", biggestSpendAmount / 1000)}K · $biggestSpendPercent%"

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
                    onEditBudget = { showBudgetDialog = true },
                )
                QuickActionRow(
                    actions = listOf(
                        QuickAction(
                            icon = Icons.Outlined.Add, 
                            label = "Add", 
                            onClick = onLogExpense,
                            iconTint = Color(0xFF00BC7D)
                        ),
                        QuickAction(
                            icon = Icons.Outlined.History, 
                            label = "All", 
                            onClick = onViewHistory,
                            iconTint = Color(0xFF3B82F6)
                        ),
                        QuickAction(
                            icon = Icons.Outlined.Category, 
                            label = "Categories", 
                            onClick = onViewHistory,
                            iconTint = Color(0xFF8B5CF6)
                        ),
                        QuickAction(
                            icon = Icons.Outlined.NotificationsActive, 
                            label = "Subscriptions", 
                            onClick = onViewSubscriptions,
                            iconTint = Color(0xFFEF4444)
                        ),
                    ),
                )
            }
        }
        screenSection {
            ExpensesInsightsSection(
                biggestSpendCategory = biggestSpendCategory,
                biggestSpendSubtitle = biggestSpendSubtitle,
                subscriptionCost = uiState.subscriptionCost,
                subscriptionCount = uiState.subscriptionCount,
                spendTrend = uiState.spendTrend,
                dailyBudget = uiState.dailyBudget,
                currencyCode = uiState.currency.code,
                onSubscriptionLongClick = onViewSubscriptions
            )
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

    if (showBudgetDialog) {
        MonthlyBudgetDialog(
            currentBudget = uiState.monthlyBudget,
            currencyCode = uiState.currency.code,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { amount ->
                viewModel.setMonthlyBudget(amount)
                showBudgetDialog = false
            },
        )
    }
}

@Composable
private fun MonthlyBudgetDialog(
    currentBudget: Double,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var input by remember {
        mutableStateOf(if (currentBudget > 0.0) currentBudget.toLong().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Monthly budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(text = "Set your spending budget for the month ($currencyCode). Leave at 0 to turn budget tracking off.")
                OutlinedTextField(
                    value = input,
                    onValueChange = { new -> input = new.filter { it.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(text = "Amount") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input.toDoubleOrNull() ?: 0.0) }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}
