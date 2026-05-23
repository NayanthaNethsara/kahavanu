package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
    label: String,
    color: Color,
    bordered: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val base = modifier
        .background(color.copy(alpha = 0.1f), CircleShape)
        .let { if (bordered) it.border(0.5.dp, color.copy(alpha = 0.25f), CircleShape) else it }
        .padding(horizontal = 8.dp, vertical = 3.dp)

    Box(modifier = base) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
