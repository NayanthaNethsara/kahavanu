package com.kahavanu.ui.expenses.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kahavanu.ui.common.TrendChart

@Composable
fun ExpensesPerformanceChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    dailyBudget: Float? = null,
    endLabel: String? = null,
) {
    TrendChart(
        points = points,
        lineColor = Color(0xFF00BC7D),
        modifier = modifier,
        budgetLine = dailyBudget,
        endLabel = endLabel,
    )
}
