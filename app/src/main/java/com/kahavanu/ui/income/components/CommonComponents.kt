package com.kahavanu.ui.income.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.income.CurrencyOption
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondaryEmerald
import com.kahavanu.ui.theme.TextTertiaryEmerald
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSize

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                spotColor = RawColors.Gray.Gray400,
                ambientColor = RawColors.Gray.Gray500,
                shape = KahavanuShapes.large
            ),
        shape = KahavanuShapes.large,
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, actionText: String? = null, badgeCount: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.lg,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (badgeCount != null) {
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Box(
                        modifier = Modifier
                            .background(RawColors.Emerald.Emerald50, CircleShape)
                            .padding(horizontal = Spacing.medium, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = TextSize.xs,
                            color = TextSecondaryEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                color = TextSecondary
            )
        }
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                fontSize = TextSize.sm,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                modifier = Modifier.padding(top = Spacing.extraSmall)
            )
        }
    }
    Spacer(modifier = Modifier.height(Spacing.large))
}
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontSize = TextSize.sm,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
    )
}

@Composable
fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
            RawColors.Emerald.Emerald500.copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(elevation = 18.dp, spotColor = Color.Black.copy(alpha = 0.25f), shape = KahavanuShapes.large)
            .background(
                if (enabled) gradient else Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                KahavanuShapes.large
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = KahavanuShapes.large
            )
            .clip(KahavanuShapes.large)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontSize = TextSize.base,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.23).sp,
        )
    }
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(6.dp, CircleShape)
            .background(Color.White.copy(alpha = 0.8f), CircleShape)
            .border(0.7.dp, RawColors.Slate.Slate200.copy(alpha = 0.7f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun GradientBlob(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    colors: List<Color>,
) {
    Box(
        modifier = modifier
            .size(size)
            .blur(80.dp)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape,
            ),
    )
}

@Composable
fun textFieldColors() =
    OutlinedTextFieldDefaults.colors(
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

@Composable
fun CurrencyToggle(
    selected: CurrencyOption,
    onSelect: (CurrencyOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .width(110.dp)
            .height(52.dp)
            .background(
                RawColors.Slate.Slate900.copy(alpha = 0.06f),
                KahavanuShapes.medium,
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CurrencyOptionButton(
            text = CurrencyOption.LKR.code,
            selected = selected == CurrencyOption.LKR,
            onClick = { onSelect(CurrencyOption.LKR) },
            modifier = Modifier.weight(1f),
        )
        CurrencyOptionButton(
            text = CurrencyOption.USD.code,
            selected = selected == CurrencyOption.USD,
            onClick = { onSelect(CurrencyOption.USD) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun CurrencyOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (selected) RawColors.Emerald.Emerald500 else Color.Transparent,
                KahavanuShapes.small,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary,
        )
    }
}

@Composable
fun CurrencyDropdown(
    selected: CurrencyOption,
    onSelect: (CurrencyOption) -> Unit,
    modifier: Modifier = Modifier,
    options: List<CurrencyOption> = CurrencyOption.values().toList()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(RawColors.Slate.Slate900.copy(alpha = 0.04f), KahavanuShapes.medium)
                .clickable { expanded = true }
                .padding(horizontal = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${selected.code} (${selected.symbol})",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                color = TextPrimary
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .width(160.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${option.code} (${option.symbol})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = TextSize.sm,
                            color = TextPrimary
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
