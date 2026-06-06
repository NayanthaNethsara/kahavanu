package com.kahavanu.ui.income.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.common.TrendChart
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.util.lastNDayLabels

@Composable
fun PerformanceChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    endLabel: String? = null,
) {
    TrendChart(
        points = points,
        lineColor = AccentIncome,
        modifier = modifier,
        endLabel = endLabel,
        dayLabels = if (points.isNotEmpty()) lastNDayLabels(points.size) else emptyList(),
    )
}
