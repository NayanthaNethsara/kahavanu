package com.kahavanu.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun appDecorativeGradientBrush(): Brush = Brush.radialGradient(
    colorStops = arrayOf(
        0.0f to DecorativeGradientStart,
        0.55f to DecorativeGradientStart.copy(alpha = 0.12f),
        1.0f to Color.Transparent,
    ),
)

fun appButtonHighlightBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.20f),
        Color.Transparent,
    ),
)