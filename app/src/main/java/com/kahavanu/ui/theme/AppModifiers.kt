package com.kahavanu.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.circularIconButton(
    backgroundColor: Color = Color.White.copy(alpha = 0.8f),
    borderColor: Color = OutlineVariant.copy(alpha = 0.7f),
    borderWidth: Dp = 0.7.dp,
    size: Dp = 40.dp,
) = this
    .size(size)
    .background(backgroundColor, CircleShape)
    .border(borderWidth, borderColor, CircleShape)
