package com.kahavanu.ui.common

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 44.dp,
    shape: Shape = KahavanuShapes.medium,
    selectedBackgroundColor: Color = MaterialTheme.extendedColors.brandAccent.copy(alpha = 0.12f),
    unselectedBackgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
    selectedBorderColor: Color = MaterialTheme.extendedColors.brandAccent,
    unselectedBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    selectedTextColor: Color = MaterialTheme.extendedColors.brandText,
    unselectedTextColor: Color = TextSecondary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    fontSize: TextUnit = TextSize.xs,
    selectedFontWeight: FontWeight = FontWeight.Bold,
    unselectedFontWeight: FontWeight = FontWeight.Medium,
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                if (selected) selectedBackgroundColor else unselectedBackgroundColor,
                shape,
            )
            .border(
                1.dp,
                if (selected) selectedBorderColor else unselectedBorderColor,
                shape,
            )
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            fontSize = fontSize,
            fontWeight = if (selected) selectedFontWeight else unselectedFontWeight,
            color = if (selected) selectedTextColor else unselectedTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.small),
        )
    }
}

@Composable
fun SelectableCard(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 72.dp,
    shape: Shape = KahavanuShapes.large,
    selectedBackgroundColor: Color = MaterialTheme.extendedColors.brandGlow.copy(alpha = 0.12f),
    unselectedBackgroundColor: Color = Color.White.copy(alpha = 0.4f),
    selectedBorderColor: Color = MaterialTheme.extendedColors.brandSoft,
    unselectedBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    selectedTitleColor: Color = MaterialTheme.colorScheme.primary,
    unselectedTitleColor: Color = TextPrimary,
    subtitleColor: Color = TextSecondary,
    titleStyle: TextStyle = MaterialTheme.typography.labelMedium,
    subtitleStyle: TextStyle = MaterialTheme.typography.labelSmall,
    titleFontSize: TextUnit = TextSize.sm,
    subtitleFontSize: TextUnit = TextSize.xs,
    selectedTitleWeight: FontWeight = FontWeight.Bold,
    defaultTitleWeight: FontWeight = FontWeight.Medium,
    subtitleLineHeight: TextUnit = 14.sp,
    titleMaxLines: Int = 1,
    contentPadding: Dp = Spacing.small,
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                if (selected) selectedBackgroundColor else unselectedBackgroundColor,
                shape,
            )
            .border(
                1.dp,
                if (selected) selectedBorderColor else unselectedBorderColor,
                shape,
            )
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = titleStyle,
                fontSize = titleFontSize,
                fontWeight = if (selected) selectedTitleWeight else defaultTitleWeight,
                color = if (selected) selectedTitleColor else unselectedTitleColor,
                textAlign = TextAlign.Center,
                maxLines = titleMaxLines,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = subtitleStyle,
                    fontSize = subtitleFontSize,
                    color = subtitleColor,
                    lineHeight = subtitleLineHeight,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
