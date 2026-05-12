package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.theme.Spacing

import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import com.kahavanu.ui.common.QuickActionButton

@Composable
fun ExpensesActionButtons(
    onLogExpense: () -> Unit,
    onViewBills: () -> Unit,
    onViewBudgets: () -> Unit,
    onViewHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Add,
            label = "Log Expense",
            onClick = onLogExpense
        )
        QuickActionButton(
            icon = Icons.Outlined.BarChart,
            label = "Budgets",
            onClick = onViewBudgets
        )
        QuickActionButton(
            icon = Icons.Outlined.ReceiptLong,
            label = "Bills",
            onClick = onViewBills
        )
        QuickActionButton(
            icon = Icons.Outlined.History,
            label = "History",
            onClick = onViewHistory
        )
    }
}
