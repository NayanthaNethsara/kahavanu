package com.kahavanu.ui.common

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize



@Composable
fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.75f),
            MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.9f),
        ),
    )

    val disabledGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.extendedColors.brandText.copy(alpha = 0.8f),
            MaterialTheme.extendedColors.brandDark.copy(alpha = 0.8f),
            MaterialTheme.extendedColors.brandText.copy(alpha = 0.8f),
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
                if (enabled) gradient else disabledGradient,
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
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.65f),
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
    focusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.extendedColors.iconMuted.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = MaterialTheme.extendedColors.iconMuted.copy(alpha = 0.7f),
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
    disabledContainerColor = Color.White.copy(alpha = 0.7f),
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledPlaceholderColor = MaterialTheme.extendedColors.iconMuted.copy(alpha = 0.7f),
    disabledTrailingIconColor = MaterialTheme.extendedColors.iconMuted,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorContainerColor = Color.White.copy(alpha = 0.7f),
)

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(65.dp)
                .shadow(
                    elevation = 20.dp,
                    spotColor = MaterialTheme.extendedColors.textTertiary,
                    ambientColor = MaterialTheme.extendedColors.iconMuted,
                    shape = KahavanuShapes.large
                )
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = KahavanuShapes.large
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = KahavanuShapes.large
                )
                .clickable(onClick = onClick)
                .padding(Spacing.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(25.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = TextSize.xs,
            color = TextSecondary,
        )
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
            )
        }
        Text(
            text = value,
            fontSize = TextSize.sm,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
