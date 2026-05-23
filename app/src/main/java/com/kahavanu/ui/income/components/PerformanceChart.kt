package com.kahavanu.ui.income.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.AccentIncome

@Composable
fun PerformanceChart(
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Point coordinates for Week 2 cursor/dot (approximately at 70% width and 35% height)
        val cursorX = widthPx * 0.72f
        val cursorY = heightPx * 0.36f

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw "Last Month" curve (Solid Slate Line)
            val lastMonthPath = Path().apply {
                moveTo(0f, canvasHeight)
                cubicTo(
                    canvasWidth * 0.25f, canvasHeight * 0.85f,
                    canvasWidth * 0.55f, canvasHeight * 0.55f,
                    canvasWidth, canvasHeight * 0.32f
                )
            }
            drawPath(
                path = lastMonthPath,
                color = Color(0xFFCBD5E1),
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Draw "Expected" curve (Dashed Slate Line)
            val expectedPath = Path().apply {
                moveTo(0f, canvasHeight)
                cubicTo(
                    canvasWidth * 0.25f, canvasHeight * 0.78f,
                    canvasWidth * 0.55f, canvasHeight * 0.42f,
                    canvasWidth, canvasHeight * 0.12f
                )
            }
            drawPath(
                path = expectedPath,
                color = Color(0xFF94A3B8).copy(alpha = 0.5f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )

            // 3. Draw "Current" curve Fill Area
            val currentFillPath = Path().apply {
                moveTo(0f, canvasHeight)
                cubicTo(
                    canvasWidth * 0.22f, canvasHeight * 0.82f,
                    canvasWidth * 0.48f, canvasHeight * 0.52f,
                    cursorX, cursorY
                )
                lineTo(cursorX, canvasHeight)
                lineTo(0f, canvasHeight)
                close()
            }
            drawPath(
                path = currentFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentIncome.copy(alpha = 0.22f),
                        Color.Transparent
                    )
                )
            )

            // 4. Draw "Current" curve Line
            val currentLinePath = Path().apply {
                moveTo(0f, canvasHeight)
                cubicTo(
                    canvasWidth * 0.22f, canvasHeight * 0.82f,
                    canvasWidth * 0.48f, canvasHeight * 0.52f,
                    cursorX, cursorY
                )
            }
            drawPath(
                path = currentLinePath,
                color = AccentIncome,
                style = Stroke(width = 3.dp.toPx())
            )

            // 5. Draw Glowing Indicator Dot at the cursor end point
            drawCircle(
                color = AccentIncome.copy(alpha = 0.25f),
                radius = 12.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(cursorX, cursorY)
            )
            drawCircle(
                color = AccentIncome,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(cursorX, cursorY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(cursorX, cursorY)
            )
        }

        // 6. Draw floating Tooltip Badge overlay positioned perfectly above the cursor dot
        val badgeWidth = 62.dp
        val badgeHeight = 18.dp
        
        // Calculate offset (x: center over dot, y: sit above dot with vertical spacing)
        val xOffset = with(androidx.compose.ui.platform.LocalDensity.current) {
            (cursorX.toDp() - (badgeWidth / 2))
        }
        val yOffset = with(androidx.compose.ui.platform.LocalDensity.current) {
            (cursorY.toDp() - badgeHeight - 8.dp)
        }

        Box(
            modifier = Modifier
                .offset(x = xOffset, y = yOffset)
                .size(width = badgeWidth, height = badgeHeight)
                .background(AccentIncome, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LKR 240K",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.15.sp,
                lineHeight = 9.sp
            )
        }
    }
}
