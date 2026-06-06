package com.kahavanu.ui.income.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.common.CellDivider
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.common.StatCell
import com.kahavanu.ui.common.compactAmount
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.AccentIncome
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun InsightsSection(
    modifier: Modifier = Modifier,
    topSourceTitle: String = "—",
    topSourceSubtitle: String = "No income yet",
    currentSavings: Double = 0.0,
    thisWeekIncome: Double = 0.0,
    weeklyAverageIncome: Double = 0.0,
    incomeTrend: List<Float> = emptyList(),
    currencyCode: String = "LKR",
) {
    val vsAverage: Int = if (weeklyAverageIncome > 0.0) {
        (((thisWeekIncome - weeklyAverageIncome) / weeklyAverageIncome) * 100).roundToInt()
    } else 0

    val savingsPositive = currentSavings >= 0.0
    val savingsColor = if (savingsPositive) AccentIncome else Color(0xFFDC2626)
    val savingsLabel = (if (savingsPositive) "" else "−") +
        compactAmount(currencyCode, abs(currentSavings).toFloat())

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        SectionHeader(
            title = "Insights",
            subtitle = "Performance breakdown"
        )

        // 1. Performance breakdown Main Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                Text(
                    text = "THIS WEEK vs WEEKLY AVERAGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // 3-column grid: this week, weekly average, and the difference.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCell(
                        modifier = Modifier.weight(1f),
                        label = "This Week",
                        value = compactAmount(currencyCode, thisWeekIncome.toFloat()),
                        valueColor = TextPrimary,
                    )
                    CellDivider()
                    StatCell(
                        modifier = Modifier.weight(1f),
                        label = "Weekly Avg",
                        value = compactAmount(currencyCode, weeklyAverageIncome.toFloat()),
                        valueColor = TextPrimary,
                    )
                    CellDivider()
                    StatCell(
                        modifier = Modifier.weight(1f),
                        label = "vs Avg",
                        value = "${if (vsAverage > 0) "+" else ""}$vsAverage%",
                        valueColor = if (vsAverage >= 0) AccentIncome else Color(0xFFDC2626),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                Text(
                    text = "LAST 7 DAYS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                // Real income trend line chart (last 7 days)
                PerformanceChart(
                    points = incomeTrend,
                    endLabel = incomeTrend.lastOrNull()?.let { compactAmount(currencyCode, it) },
                )
            }
        }

        // 2. Side-by-side Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card 1: Top Source Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(134.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6), // Premium Violet
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TOP SOURCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = topSourceTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFF97316), CircleShape) // Semantic orange
                        )
                        Text(
                            text = topSourceSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Card 2: Current Savings Card (all-time received income minus logged expenses)
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(134.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Savings,
                        contentDescription = null,
                        tint = savingsColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "CURRENT SAVINGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = savingsLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = savingsColor,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(savingsColor, CircleShape)
                        )
                        Text(
                            text = "Income − Expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

