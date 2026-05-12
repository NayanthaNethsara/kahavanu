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
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.PendingExpenseMatch
import com.kahavanu.ui.expenses.RecentExpense
import com.kahavanu.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun MatchAndCategorizeSection(
    items: List<PendingExpenseMatch>,
    currency: CurrencyOption,
) {
    MatchingSection(
        title = "Match & Categorize",
        subtitle = "Uncategorized expenses from SMS",
        items = items.map { item ->
            MatchItemState(
                id = item.id,
                title = "Unknown Merchant",
                subtitle = item.receivedAtLabel,
                amount = formatAmountNoDecimals(item.amount, currency.code),
                matchPercent = item.confidencePercent,
                likelyFor = "Likely ${item.category}",
                icon = categoryIcon(item.category),
                iconTint = categoryColor(item.category),
                primaryActionLabel = "Categorize",
                secondaryActionLabel = "Edit"
            )
        },
        onPrimaryAction = { },
        onSecondaryAction = { },
        onDismiss = { }
    )
}

@Composable
fun ByCategorySection(
    categories: List<ExpenseCategorySummary>,
    currency: CurrencyOption,
) {
    val total = categories.sumOf { it.amount }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Column {
            Text(
                text = "By Category",
                style = MaterialTheme.typography.titleMedium,
                fontSize = TextSize.base,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Spending breakdown",
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
            )
        }

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
                        HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.4f))
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
                    fontSize = 10.sp,
                    color = TextSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(RawColors.Slate.Slate100, RoundedCornerShape(99.dp)),
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
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = TextSize.base,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Latest transactions",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                )
            }
            Text(
                text = "View all ›",
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                color = RawColors.Emerald.Emerald600,
                modifier = Modifier
                    .background(RawColors.Emerald.Emerald50, RoundedCornerShape(99.dp))
                    .clickable(onClick = onViewAll)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        categoryColor(expense.category).copy(alpha = 0.14f),
                                        RoundedCornerShape(10.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = categoryIcon(expense.category),
                                    contentDescription = null,
                                    tint = categoryColor(expense.category),
                                    modifier = Modifier.size(14.dp),
                                )
                            }

                            Spacer(modifier = Modifier.width(Spacing.small))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = TextSize.sm,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = buildRecentSubtitle(expense),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                )
                            }

                            Text(
                                text = "-${formatAmountNoDecimals(expense.amount, currency.code)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = TextSize.sm,
                                color = categoryColor(expense.category),
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        if (index != expenses.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.medium),
                                color = RawColors.Slate.Slate200.copy(alpha = 0.4f),
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
