package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
