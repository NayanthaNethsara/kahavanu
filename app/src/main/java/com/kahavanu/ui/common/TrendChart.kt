package com.kahavanu.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** Percentage change from the first non-zero value to the last value in a series. */
fun trendPercent(series: List<Float>): Int {
    val first = series.firstOrNull { it > 0f } ?: return 0
    val last = series.lastOrNull() ?: return 0
    return (((last - first) / first) * 100).roundToInt()
}

/** Compact money label for chart badges, e.g. "LKR 82K" / "USD 1.2M". */
fun compactAmount(currencyCode: String, value: Float): String {
    val v = value.toDouble()
    return when {
        v >= 1_000_000 -> "$currencyCode ${"%.1f".format(v / 1_000_000)}M"
        v >= 1_000 -> "$currencyCode ${(v / 1_000).roundToInt()}K"
        else -> "$currencyCode ${v.roundToInt()}"
    }
}

/**
 * A small line/area chart driven by real data [points] (chronological values).
 *
 * - Draws the trend line plus a soft gradient fill below it.
 * - Marks the most recent point with a dot and an optional [endLabel] badge.
 * - Optionally draws a dashed [budgetLine] reference (e.g. the daily budget).
 * - When [dayLabels] is supplied (one per point), draws faint vertical gridlines at each
 *   point and the labels (e.g. weekdays Mon…Sun) along the bottom.
 *
 * When there are fewer than two points it draws a flat baseline instead of a fake curve.
 */
@Composable
fun TrendChart(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    budgetLine: Float? = null,
    budgetColor: Color = Color(0xFFDC2626),
    endLabel: String? = null,
    dayLabels: List<String> = emptyList(),
) {
    val showLabels = dayLabels.size == points.size && points.isNotEmpty()
    val gridColor = lineColor.copy(alpha = 0.12f)
    val axisLabelColor = Color(0xFF94A3B8)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(if (showLabels) 146.dp else 130.dp),
    ) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        // Reserve a strip at the bottom for the weekday labels so they don't overlap the plot.
        val labelStripPx = if (showLabels) with(density) { 16.dp.toPx() } else 0f

        val maxValue = ((points.maxOrNull() ?: 0f).coerceAtLeast(budgetLine ?: 0f))
        val hasData = points.size >= 2 && maxValue > 0f

        // Maps a value and an index to canvas coordinates. The drawable band leaves
        // headroom at the top (for the dot/badge) and a small floor at the bottom.
        // [h] is the plotting height (total height minus the bottom label strip).
        fun yOf(value: Float, h: Float): Float = h * 0.96f - (value / maxValue) * (h * 0.80f)
        fun xOf(index: Int, w: Float): Float =
            if (points.size <= 1) w * 0.5f else (index.toFloat() / (points.size - 1)) * w

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height - labelStripPx

            // Vertical gridlines + weekday labels, drawn behind the trend line.
            if (showLabels) {
                val labelPaint = android.graphics.Paint().apply {
                    color = axisLabelColor.toArgb()
                    textSize = 9.sp.toPx()
                    isAntiAlias = true
                }
                points.indices.forEach { i ->
                    val x = xOf(i, w)
                    drawLine(
                        color = gridColor,
                        start = Offset(x, h * 0.10f),
                        end = Offset(x, h * 0.96f),
                        strokeWidth = 1.dp.toPx(),
                    )
                    labelPaint.textAlign = when (i) {
                        0 -> android.graphics.Paint.Align.LEFT
                        points.lastIndex -> android.graphics.Paint.Align.RIGHT
                        else -> android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        dayLabels[i],
                        x,
                        size.height - 3.dp.toPx(),
                        labelPaint,
                    )
                }
            }

            if (!hasData) {
                drawLine(
                    color = lineColor.copy(alpha = 0.25f),
                    start = Offset(0f, h * 0.7f),
                    end = Offset(w, h * 0.7f),
                    strokeWidth = 2.dp.toPx(),
                )
                return@Canvas
            }

            val offsets = points.mapIndexed { i, v -> Offset(xOf(i, w), yOf(v, h)) }

            // Gradient fill under the line.
            val fillPath = Path().apply {
                moveTo(offsets.first().x, h)
                offsets.forEach { lineTo(it.x, it.y) }
                lineTo(offsets.last().x, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.22f), Color.Transparent),
                ),
            )

            // Trend line.
            val linePath = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx()))

            // Optional budget reference line.
            if (budgetLine != null && budgetLine > 0f) {
                val by = yOf(budgetLine, h)
                drawLine(
                    color = budgetColor.copy(alpha = 0.4f),
                    start = Offset(0f, by),
                    end = Offset(w, by),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                )
            }

            // End-point marker.
            val last = offsets.last()
            drawCircle(color = lineColor.copy(alpha = 0.25f), radius = 12.dp.toPx(), center = last)
            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = last)
            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = last)
        }

        if (hasData && endLabel != null) {
            val badgeWidth = 70.dp
            val badgeHeight = 18.dp
            val lastX = xOf(points.lastIndex, widthPx)
            val lastY = yOf(points.last(), heightPx - labelStripPx)
            val xOffset = with(density) { lastX.toDp() - (badgeWidth / 2) }
            val yOffset = with(density) { lastY.toDp() - badgeHeight - 8.dp }

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(width = badgeWidth, height = badgeHeight)
                    .background(lineColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = endLabel,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.15.sp,
                    lineHeight = 9.sp,
                    maxLines = 1,
                )
            }
        }
    }
}
