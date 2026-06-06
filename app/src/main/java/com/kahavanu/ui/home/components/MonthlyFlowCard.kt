package com.kahavanu.ui.home.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.compactAmount
import com.kahavanu.ui.home.MonthlyFlowPoint
import com.kahavanu.ui.theme.AccentExpense
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

/**
 * Home cash-flow card: grouped bars comparing income (green) vs. expenses (orange) for each of
 * the last few months. A net summary for the window sits in the header.
 */
@Composable
fun MonthlyFlowCard(
    points: List<MonthlyFlowPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val totalIncome = points.sumOf { it.income }
    val totalExpense = points.sumOf { it.expense }
    val net = totalIncome - totalExpense
    val netPositive = net >= 0.0
    val netColor = if (netPositive) AccentIncome else AccentExpense
    val netLabel = (if (netPositive) "+" else "−") +
        compactAmount(currencyCode, kotlin.math.abs(net).toFloat())

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "CASH FLOW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last ${points.size} months",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NET",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = netLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = netColor,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            MonthlyFlowBars(
                points = points,
                incomeColor = AccentIncome,
                expenseColor = AccentExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                LegendDot(color = AccentIncome, label = "Income")
                LegendDot(color = AccentExpense, label = "Expenses")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun MonthlyFlowBars(
    points: List<MonthlyFlowPoint>,
    incomeColor: Color,
    expenseColor: Color,
    modifier: Modifier = Modifier,
) {
    val labelColor = TextSecondary
    val gridColor = TextSecondary.copy(alpha = 0.15f)
    val maxValue = points.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 0.0
    val hasData = points.isNotEmpty() && maxValue > 0.0

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val labelStrip = 18.dp.toPx()
        val baseline = h - labelStrip
        val maxBarH = baseline * 0.92f

        // Baseline axis.
        drawLine(
            color = gridColor,
            start = Offset(0f, baseline),
            end = Offset(w, baseline),
            strokeWidth = 1.dp.toPx(),
        )

        if (!hasData) return@Canvas

        val labelPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 9.sp.toPx()
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val slot = w / points.size
        val barW = slot * 0.24f
        val gap = slot * 0.06f
        val radius = CornerRadius(barW * 0.4f, barW * 0.4f)

        points.forEachIndexed { i, point ->
            val cx = slot * i + slot / 2f
            val incomeX = cx - barW - gap / 2f
            val expenseX = cx + gap / 2f
            val incomeH = ((point.income / maxValue).toFloat()) * maxBarH
            val expenseH = ((point.expense / maxValue).toFloat()) * maxBarH

            // A faint full-height track keeps short/empty months visible.
            drawRoundRect(
                color = gridColor,
                topLeft = Offset(incomeX, baseline - maxBarH),
                size = Size(barW, maxBarH),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = gridColor,
                topLeft = Offset(expenseX, baseline - maxBarH),
                size = Size(barW, maxBarH),
                cornerRadius = radius,
            )

            if (incomeH > 0f) {
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(incomeX, baseline - incomeH),
                    size = Size(barW, incomeH),
                    cornerRadius = radius,
                )
            }
            if (expenseH > 0f) {
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(expenseX, baseline - expenseH),
                    size = Size(barW, expenseH),
                    cornerRadius = radius,
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                point.label,
                cx,
                h - 4.dp.toPx(),
                labelPaint,
            )
        }
    }
}
