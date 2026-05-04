package com.kahavanu.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun appDecorativeGradientBrush(): Brush = Brush.radialGradient(
    colorStops = arrayOf(
        0.0f to OnboardingGradientStart,
        0.55f to OnboardingGradientStart.copy(alpha = 0.12f),
        1.0f to Color.Transparent,
    ),
)

fun appButtonHighlightBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.20f),
        Color.Transparent,
    ),
)