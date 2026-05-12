package com.kahavanu.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    shadowElevation: Dp = 4.dp,
    backgroundColor: Color = Color.White,
    borderColor: Color = RawColors.Slate.Slate200.copy(alpha = 0.7f),
    borderWidth: Dp = 0.7.dp,
    tint: Color = TextSecondary,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = shadowElevation,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f),
            )
            .background(backgroundColor, CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 18.dp,
                spotColor = Color.Black.copy(alpha = 0.25f),
                shape = KahavanuShapes.large,
            )
            .background(
                if (enabled) gradient else Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                KahavanuShapes.large,
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.1f),
                    ),
                ),
                shape = KahavanuShapes.large,
            )
            .clip(KahavanuShapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontSize = TextSize.base,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.23).sp,
        )
    }
}

@Composable
fun GradientBlob(
    modifier: Modifier,
    size: Dp,
    colors: List<Color>,
    blurRadius: Dp = 80.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(blurRadius)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape,
            ),
    )
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.7f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
    focusedBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.9f),
    unfocusedBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.9f),
    focusedTextColor = RawColors.Slate.Slate900,
    unfocusedTextColor = RawColors.Slate.Slate900,
    focusedPlaceholderColor = RawColors.Slate.Slate500.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = RawColors.Slate.Slate500.copy(alpha = 0.7f),
    disabledBorderColor = RawColors.Slate.Slate200.copy(alpha = 0.9f),
    disabledContainerColor = Color.White.copy(alpha = 0.7f),
    disabledTextColor = RawColors.Slate.Slate900,
    disabledPlaceholderColor = RawColors.Slate.Slate500.copy(alpha = 0.7f),
    disabledTrailingIconColor = RawColors.Slate.Slate500,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorContainerColor = Color.White.copy(alpha = 0.7f),
)
