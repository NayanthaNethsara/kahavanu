package com.kahavanu.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExpenseHistoryScreen(
    onBack: () -> Unit,
    viewModel: ExpenseHistoryViewModel = hiltViewModel(),
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = Spacing.extraLarge, vertical = 30.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.medium))
                Column {
                    Text(
                        text = "EXPENSE HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                        letterSpacing = 0.72.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "All your transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = TextSize.lg,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.8).sp,
                        color = TextPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "No expenses yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "Log your first expense to see it here",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.6f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    items(expenses) { expense ->
                        ExpenseHistoryCard(expense = expense)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseHistoryCard(expense: ExpenseLogEntry) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val date = Instant.ofEpochMilli(expense.spentAtEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(dateFormatter)

    val categoryColor = when (expense.category) {
        "Essentials" -> RawColors.Rose.Rose500
        "Transport" -> RawColors.Blue.Blue500
        "Lifestyle" -> RawColors.Amber.Amber500
        "Subscriptions" -> RawColors.Indigo.Indigo400
        else -> RawColors.Gray.Gray400
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = TextSize.base,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = TextSecondary,
                    )
                    if (!expense.merchant.isNullOrBlank()) {
                        Text(
                            text = "• ${expense.merchant}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = TextSize.xs,
                            color = TextSecondary,
                        )
                    }
                }
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.sm,
                    color = TextSecondary.copy(alpha = 0.7f),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
            ) {
                Text(
                    text = "-${expense.currency} ${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = TextSize.base,
                    fontWeight = FontWeight.SemiBold,
                    color = RawColors.Rose.Rose500,
                )
                Text(
                    text = expense.paymentMethod ?: "Cash",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                )
            }
        }
    }
}
