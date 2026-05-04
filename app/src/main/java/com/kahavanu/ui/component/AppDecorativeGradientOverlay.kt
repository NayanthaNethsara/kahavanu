package com.kahavanu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.appDecorativeGradientBrush

@Composable
fun AppDecorativeGradientOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(280.dp)
            .blur(60.dp)
            .background(
                brush = appDecorativeGradientBrush(),
                shape = CircleShape,
            ),
    )
}