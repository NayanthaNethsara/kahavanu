package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun ExpensesActionButtons(
    onLogExpense: () -> Unit,
    onViewBills: () -> Unit,
    onViewBudgets: () -> Unit,
    onViewHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
            label = "Log Expense",
            onClick = onLogExpense,
        )
        QuickActionButton(
            icon = Icons.Outlined.ReceiptLong,
            label = "Bills",
            onClick = onViewBills,
        )
        QuickActionButton(
            icon = Icons.Outlined.PieChartOutline,
            label = "Budgets",
            onClick = onViewBudgets,
        )
        QuickActionButton(
            icon = Icons.Outlined.History,
            label = "History",
            onClick = onViewHistory,
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(65.dp)
                .shadow(
                    elevation = 20.dp,
                    spotColor = RawColors.Gray.Gray400,
                    ambientColor = RawColors.Gray.Gray500,
                    shape = KahavanuShapes.large,
                )
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = KahavanuShapes.large,
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = KahavanuShapes.large,
                )
                .clickable(onClick = onClick)
                .padding(Spacing.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(25.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = TextSize.xs,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}
