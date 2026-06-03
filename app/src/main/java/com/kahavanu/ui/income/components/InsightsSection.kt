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
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.SectionHeader
import com.kahavanu.ui.common.compactAmount
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.AccentIncome
import kotlin.math.roundToInt

@Composable
fun InsightsSection(
    modifier: Modifier = Modifier,
    topSourceTitle: String = "Salary",
    topSourceSubtitle: String = "LKR 120K · 43%",
    nextPaymentsTotal: Double = 58500.0,
    nextPaymentsCount: Int = 2,
    thisWeekIncome: Double = 0.0,
    weeklyAverageIncome: Double = 0.0,
    incomeTrend: List<Float> = emptyList(),
    currencyCode: String = "LKR",
) {
    val vsAverage: Int = if (weeklyAverageIncome > 0.0) {
        (((thisWeekIncome - weeklyAverageIncome) / weeklyAverageIncome) * 100).roundToInt()
    } else 0

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

            // Card 2: Next 7 Days Card
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
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6), // Premium Blue
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NEXT 7 DAYS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "LKR ${String.format("%,.0f", nextPaymentsTotal)}",
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
                                .background(Color(0xFF3B82F6), CircleShape) // Matching blue
                        )
                        Text(
                            text = "$nextPaymentsCount payment" + if (nextPaymentsCount != 1) "s" else "",
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

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 17.sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    )
}
