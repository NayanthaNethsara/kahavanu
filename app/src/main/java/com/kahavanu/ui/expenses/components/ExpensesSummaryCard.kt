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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.common.AppSegmentedToggle
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.ExpensePeriod
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ExpensesSummaryCard(
    selectedPeriod: ExpensePeriod,
    onPeriodChange: (ExpensePeriod) -> Unit,
    currency: CurrencyOption,
    totalSpent: Double,
    budgetLimit: Double,
    categorySummaries: List<ExpenseCategorySummary>,
    monthLabel: String,
) {
    val periodOptions = ExpensePeriod.values().toList()
    val totalSpentText = formatAmount(totalSpent, currency.code)
    val budgetLeft = budgetLimit - totalSpent
    val overBudget = budgetLeft < 0
    val progress = if (budgetLimit > 0.0) min(1f, (totalSpent / budgetLimit).toFloat()) else 0f
    val progressPercent = "${(progress * 100).roundToInt()}%"
    val statusText = if (overBudget) {
        "Over budget by ${formatAmount(kotlin.math.abs(budgetLeft), currency.code)}"
    } else {
        "Budget left ${formatAmount(budgetLeft, currency.code)}"
    }
    val statusColor = if (overBudget) RawColors.Red.Red600 else RawColors.Emerald.Emerald600
    val headerLabel = when (selectedPeriod) {
        ExpensePeriod.Week -> "Total spent · This week"
        ExpensePeriod.Month -> "Total spent · $monthLabel"
        ExpensePeriod.Year -> "Total spent · ${java.time.Year.now().value}"
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = headerLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = TextSize.sm,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f, fill = false),
                )
                AppSegmentedToggle(
                    items = periodOptions,
                    selectedItem = selectedPeriod,
                    onSelect = onPeriodChange,
                    labelFor = { it.label },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .height(34.dp),
                    height = 34.dp,
                    shape = CircleShape,
                    containerColor = RawColors.Rose.Rose100.copy(alpha = 0.6f),
                    indicatorColor = RawColors.Rose.Rose500,
                    indicatorShadow = 4.dp,
                    selectedTextColor = Color.White,
                    unselectedTextColor = RawColors.Rose.Rose700,
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    itemWidth = 64.dp,
                )
            }

            Text(
                text = totalSpentText,
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextSize.xxxl,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextSize.xs,
                    color = statusColor,
                )
                Text(
                    text = progressPercent,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = TextSize.sm,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = RawColors.Rose.Rose500,
                trackColor = RawColors.Slate.Slate100.copy(alpha = 0.6f),
                drawStopIndicator = {},
            )

            if (categorySummaries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = RawColors.Slate.Slate200.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    categorySummaries.forEach { item ->
                        SummaryItem(
                            label = item.label,
                            value = formatAmount(item.amount, currency.code),
                            color = item.color,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape),
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextSize.xs,
                color = TextSecondary,
            )
        }
        Text(
            text = value,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
    }
}
