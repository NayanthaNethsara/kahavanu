package com.kahavanu.ui.expenses.components

import com.kahavanu.ui.util.formatAmount
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SummaryItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.expenses.ExpenseCategorySummary
import com.kahavanu.ui.expenses.ExpensePeriod
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import kotlin.math.roundToInt

@Composable
fun ExpensesSummaryCard(
    selectedPeriod: ExpensePeriod,
    currency: CurrencyOption,
    totalSpent: Double,
    budgetLimit: Double,
    categorySummaries: List<ExpenseCategorySummary>,
    monthLabel: String,
) {
    val activeSummaries = categorySummaries.filter { it.amount > 0.0 }
    val totalForDonut = activeSummaries.sumOf { it.amount }
    val normalizedSummaries = activeSummaries.ifEmpty {
        categorySummaries
    }

    val budgetRatio = if (budgetLimit > 0.0) {
        ((totalSpent / budgetLimit) * 100).roundToInt()
    } else {
        0
    }

    val headerLabel = when (selectedPeriod) {
        ExpensePeriod.Week -> "Total spent · This week"
        ExpensePeriod.Month -> "Total spent · $monthLabel"
        ExpensePeriod.Year -> "Total spent · ${java.time.Year.now().value}"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large),
        ) {
            Text(
                text = headerLabel,
                style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            Text(
                text = formatAmount(totalSpent, currency.code, decimals = 0),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = TextSize.xxxl,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                letterSpacing = (-0.18).sp,
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = "Budget: ${formatAmount(budgetLimit, currency.code, decimals = 0)} · $budgetRatio% used",
                style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            )

            if (activeSummaries.size >= 4) {
                Spacer(modifier = Modifier.height(Spacing.small))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DonutChart(
                        summaries = normalizedSummaries,
                        total = totalForDonut,
                        modifier = Modifier.size(100.dp),
                    )
                    CategoryLegend(
                        summaries = normalizedSummaries,
                        total = totalForDonut,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else if (activeSummaries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.large))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    activeSummaries.forEach { summary ->
                        SummaryItem(
                            label = summary.label,
                            value = formatAmount(summary.amount, currency.code, decimals = 0),
                            color = summary.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    summaries: List<ExpenseCategorySummary>,
    total: Double,
    modifier: Modifier = Modifier,
) {
    val totalValue = if (total <= 0.0) 1.0 else total

    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 48f, cap = StrokeCap.Butt)
        var startAngle = -90f

        summaries.forEach { summary ->
            val ratio = (summary.amount / totalValue).toFloat().coerceAtLeast(0f)
            val sweepAngle = if (summary.amount == 0.0) 0f else ratio * 360f
            if (sweepAngle > 0f) {
                // Add a small gap between sections if there's enough space
                val gap = if (sweepAngle > 4f) 2f else 0f
                drawArc(
                    color = summary.color,
                    startAngle = startAngle + (gap / 2),
                    sweepAngle = sweepAngle - gap,
                    useCenter = false,
                    style = stroke,
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun CategoryLegend(
    summaries: List<ExpenseCategorySummary>,
    total: Double,
    modifier: Modifier = Modifier,
) {
    val totalValue = if (total <= 0.0) 1.0 else total
    val left = summaries.filterIndexed { index, _ -> index % 2 == 0 }
    val right = summaries.filterIndexed { index, _ -> index % 2 == 1 }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        ) {
            left.forEach { summary ->
                LegendItem(summary = summary, percent = (summary.amount / totalValue * 100).roundToInt())
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        ) {
            right.forEach { summary ->
                LegendItem(summary = summary, percent = (summary.amount / totalValue * 100).roundToInt())
            }
        }
    }
}

@Composable
private fun LegendItem(
    summary: ExpenseCategorySummary,
    percent: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(8.dp)
                .background(summary.color, RoundedCornerShape(99.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = summary.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                color = TextPrimary,
        )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}


