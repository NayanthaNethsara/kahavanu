package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.ExpensePeriod
import com.kahavanu.ui.expenses.RecentExpense
import com.kahavanu.ui.expenses.UpcomingBill
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.TextSize
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun SpendingInsightsSection(
    selectedPeriod: ExpensePeriod,
    currency: CurrencyOption,
    totalSpent: Double,
    categories: List<ExpenseCategorySummary>,
    recentCount: Int,
    upcomingCount: Int,
) {
    val periodDays = when (selectedPeriod) {
        ExpensePeriod.Week -> 7
        ExpensePeriod.Month -> YearMonth.now().lengthOfMonth()
        ExpensePeriod.Year -> 365
    }
    val dailyAverage = if (periodDays > 0) totalSpent / periodDays else 0.0
    val topCategory = categories.maxByOrNull { it.amount }
    val topShare = if (topCategory != null && totalSpent > 0) {
        ((topCategory.amount / totalSpent) * 100).roundToInt()
    } else 0

    Column {
        SectionHeader(
            title = "Spending insights",
            subtitle = "Trends at a glance",
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                InsightRow(
                    title = "Daily average",
                    value = formatAmount(dailyAverage, currency.code),
                    subtitle = "Based on ${selectedPeriod.label.lowercase()} spend",
                )
                Spacer(modifier = Modifier.height(Spacing.medium))
                InsightRow(
                    title = "Largest category",
                    value = topCategory?.label ?: "No data",
                    subtitle = if (topShare > 0) "$topShare% of total" else "Add expenses to see this",
                )
                Spacer(modifier = Modifier.height(Spacing.medium))
                InsightRow(
                    title = "Activity",
                    value = "$recentCount transactions",
                    subtitle = "$upcomingCount bills due soon",
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    title: String,
    value: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.xs,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontSize = TextSize.lg,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            fontSize = TextSize.xs,
            color = TextTertiary,
        )
    }
}

@Composable
fun UpcomingBillsSection(
    bills: List<UpcomingBill>,
    currency: CurrencyOption,
    onViewAll: () -> Unit,
) {
    Column {
        SectionHeader(
            title = "Upcoming bills",
            subtitle = "Due soon and recurring",
            actionText = "View all",
            badgeCount = bills.takeIf { it.isNotEmpty() }?.size?.toString(),
            onActionClick = onViewAll,
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (bills.isEmpty()) {
                Text(
                    text = "No bills due right now",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                    modifier = Modifier.padding(Spacing.large),
                )
            } else {
                bills.forEachIndexed { index, bill ->
                    UpcomingBillRow(bill = bill, currency = currency)
                    if (index != bills.lastIndex) {
                        HorizontalDivider(color = RawColors.Slate.Slate900.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingBillRow(
    bill: UpcomingBill,
    currency: CurrencyOption,
) {
    val dueText = dueLabel(bill.dueAtEpochMillis)
    val isOverdue = bill.dueAtEpochMillis < System.currentTimeMillis()
    val badgeColor = if (isOverdue) RawColors.Rose.Rose100 else RawColors.Slate.Slate100
    val badgeTextColor = if (isOverdue) RawColors.Rose.Rose700 else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = bill.title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = TextSize.base,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = TextSize.xs,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Text(
            text = formatAmount(bill.amount, currency.code),
            style = MaterialTheme.typography.titleMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = if (isOverdue) RawColors.Rose.Rose600 else TextPrimary,
        )
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
            title = "Recent expenses",
            subtitle = "Latest payments",
            actionText = "View all",
            onActionClick = onViewAll,
        )
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
                expenses.forEachIndexed { index, expense ->
                    ExpenseLogItem(expense = expense, currency = currency)
                    if (index != expenses.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.large),
                            color = RawColors.Slate.Slate900.copy(alpha = 0.06f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseLogItem(
    expense: RecentExpense,
    currency: CurrencyOption,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = RawColors.Rose.Rose500.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = expenseIconFor(expense.category),
                contentDescription = null,
                tint = RawColors.Rose.Rose600,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = TextSize.base,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                letterSpacing = (-0.07).sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildExpenseSubtitle(expense),
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
                letterSpacing = 0.06.sp,
            )
        }

        Text(
            text = "- ${formatAmount(expense.amount, currency.code)}",
            style = MaterialTheme.typography.titleMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = RawColors.Rose.Rose600,
            letterSpacing = (-0.29).sp,
        )
    }
}

private fun buildExpenseSubtitle(expense: RecentExpense): String {
    val parts = mutableListOf<String>()
    if (!expense.merchant.isNullOrBlank()) {
        parts.add(expense.merchant)
    }
    parts.add(formatDate(expense.spentAtEpochMillis))
    return parts.joinToString(" - ")
}

private fun expenseIconFor(category: String): ImageVector {
    return when (category.lowercase()) {
        "essentials" -> Icons.Outlined.ShoppingBag
        "lifestyle" -> Icons.Outlined.Restaurant
        "transport" -> Icons.Outlined.LocalGasStation
        "subscriptions" -> Icons.Outlined.Subscriptions
        else -> Icons.AutoMirrored.Outlined.ReceiptLong
    }
}
