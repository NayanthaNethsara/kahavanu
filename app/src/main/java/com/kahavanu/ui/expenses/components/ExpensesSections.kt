package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.MatchItemState
import com.kahavanu.ui.common.MatchingSection
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.PendingExpenseMatch
import com.kahavanu.ui.expenses.RecentExpense
import com.kahavanu.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun MatchAndCategorizeSection(
    items: List<PendingExpenseMatch>,
    currency: CurrencyOption,
    onConfirm: (String) -> Unit = {},
    onDismiss: (String) -> Unit = {},
) {
    if (items.isEmpty()) return

    MatchingSection(
        title = "Match & Categorize",
        subtitle = "Expenses detected from SMS",
        items = items.map { item ->
            MatchItemState(
                id = item.id,
                title = item.title,
                subtitle = item.receivedAtLabel,
                amount = formatAmountNoDecimals(item.amount, currency.code),
                matchPercent = item.confidencePercent,
                likelyFor = "Likely ${item.category}",
                icon = categoryIcon(item.category),
                iconTint = categoryColor(item.category),
                primaryActionLabel = "Confirm",
                secondaryActionLabel = "Edit",
            )
        },
        onPrimaryAction = { onConfirm(it.id) },
        onSecondaryAction = { onConfirm(it.id) },
        onDismiss = { onDismiss(it.id) },
    )
}

@Composable
fun ByCategorySection(
    categories: List<ExpenseCategorySummary>,
    currency: CurrencyOption,
) {
    val total = categories.sumOf { it.amount }

    Column {
        SectionHeader(
            title = "By Category",
            subtitle = "Spending breakdown"
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                categories.forEachIndexed { index, summary ->
                    CategorySpendItem(
                        summary = summary,
                        total = total,
                        currency = currency,
                    )
                    if (index != categories.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySpendItem(
    summary: ExpenseCategorySummary,
    total: Double,
    currency: CurrencyOption,
) {
    val percent = if (total > 0.0) ((summary.amount / total) * 100).roundToInt() else 0

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(summary.color.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = categoryIcon(summary.label),
                        contentDescription = null,
                        tint = summary.color,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = summary.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    color = TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmountNoDecimals(summary.amount, currency.code),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    color = summary.color,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(summary.color, RoundedCornerShape(99.dp)),
            )
        }
    }
}

@Composable
fun RecentExpensesSection(
    expenses: List<RecentExpense>,
    currency: CurrencyOption,
    onViewAll: () -> Unit,
) {
    Column {
        SectionHeader(
            title = "Recent Expenses",
            subtitle = "Latest transactions",
            actionText = "View all ›",
            onActionClick = onViewAll
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (expenses.isEmpty()) {
                Text(
                    text = "No expenses logged yet",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                    modifier = Modifier.padding(Spacing.large),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.small),
                ) {
                    expenses.forEachIndexed { index, expense ->
                        ExpenseListItem(
                            title = expense.title,
                            category = expense.category,
                            amount = expense.amount,
                            currencyCode = currency.code,
                            spentAtEpochMillis = expense.spentAtEpochMillis,
                            merchant = expense.merchant,
                        )
                        if (index != expenses.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.medium),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildRecentSubtitle(expense: RecentExpense): String {
    val date = formatDate(expense.spentAtEpochMillis)
    return if (expense.merchant.isNullOrBlank()) {
        "${expense.category} · $date"
    } else {
        "${expense.merchant} · $date"
    }
}
