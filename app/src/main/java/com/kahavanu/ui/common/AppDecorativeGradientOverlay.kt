package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.DecorativeTokens
import com.kahavanu.ui.theme.appDecorativeGradientBrush

@Composable
fun AppDecorativeGradientOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(DecorativeTokens.gradientSize)
            .blur(DecorativeTokens.gradientBlur)
            .background(
                brush = appDecorativeGradientBrush(),
                shape = CircleShape,
            ),
    )
}

@Composable
fun AmbientGlow(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 80.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(blurRadius)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent)
                ),
                shape = CircleShape
            )
    )
}

