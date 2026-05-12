package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.PendingExpenseMatch
import com.kahavanu.ui.expenses.RecentExpense
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlin.math.roundToInt

@Composable
fun MatchAndCategorizeSection(
    items: List<PendingExpenseMatch>,
    currency: CurrencyOption,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Match & Categorize",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = TextSize.base,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Uncategorized expenses from SMS",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.xs,
                    color = TextSecondary,
                )
            }
            if (items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(RawColors.Emerald.Emerald100, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = items.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = RawColors.Emerald.Emerald600,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            items.take(2).forEach { item ->
                MatchCard(item = item, currency = currency)
            }
        }
    }
}

@Composable
private fun MatchCard(
    item: PendingExpenseMatch,
    currency: CurrencyOption,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.receivedAtLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextSecondary,
                )
                Box(
                    modifier = Modifier
                        .background(RawColors.Emerald.Emerald100, RoundedCornerShape(99.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "${item.confidencePercent}% MATCH",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = RawColors.Emerald.Emerald600,
                    )
                }
            }

            Text(
                text = formatAmountNoDecimals(item.amount, currency.code),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RawColors.Slate.Slate100.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Icon(
                    imageVector = categoryIcon(item.category),
                    contentDescription = null,
                    tint = categoryColor(item.category),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Likely ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextPrimary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(RawColors.Emerald.Emerald500, RoundedCornerShape(99.dp))
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Categorize",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = Color.White,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(99.dp))
                        .border(0.7.dp, RawColors.Slate.Slate200, RoundedCornerShape(99.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.White.copy(alpha = 0.75f), CircleShape)
                        .border(0.7.dp, RawColors.Slate.Slate200, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
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
                    style = MaterialTheme.typography.bodyMedium,
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

private fun categoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Color(0xFFF97316)
        "transport" -> RawColors.Blue.Blue500
        "utilities" -> RawColors.Violet.Violet500
        "shopping", "lifestyle" -> RawColors.Rose.Rose500
        "health" -> RawColors.Red.Red500
        "fun", "subscriptions" -> RawColors.Emerald.Emerald500
        else -> RawColors.Slate.Slate400
    }
}

private fun categoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Icons.Outlined.LocalPizza
        "transport" -> Icons.Outlined.LocalTaxi
        "utilities" -> Icons.Outlined.Bolt
        "shopping", "lifestyle" -> Icons.Outlined.ShoppingBag
        "health" -> Icons.Outlined.LocalHospital
        "fun", "subscriptions" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.TipsAndUpdates
    }
}

private fun formatAmountNoDecimals(amount: Double, currencyCode: String): String {
    return "$currencyCode ${String.format(java.util.Locale.getDefault(), "%,.0f", amount)}"
}
