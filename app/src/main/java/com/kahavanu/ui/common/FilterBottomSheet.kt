package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.SurfaceIconBorder
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

/**
 * Drag-handled modal bottom sheet that hosts arbitrary filter sections in
 * its [content] slot. Use [FilterSection] + [FilterChips] to compose
 * category / date / payment-method pickers, and [AmountRangeSection] for
 * numeric range inputs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    subtitle: String? = null,
    onClear: (() -> Unit)? = null,
    onApply: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = Spacing.extraLarge)
                .padding(bottom = Spacing.large)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.lg,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    letterSpacing = (-0.4).sp,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = TextSize.sm,
                        color = TextSecondary,
                    )
                }
            }

            content()

            if (onClear != null || onApply != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    if (onClear != null) {
                        AppTextButton(
                            text = "Clear",
                            onClick = onClear,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (onApply != null) {
                        Button(
                            onClick = onApply,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("Apply", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.7.sp,
            color = TextSecondary,
        )
        content()
    }
}

/** Wrapping grid of selectable tag chips. Chips flow to the next line as
 *  needed so the sheet grows vertically instead of scrolling horizontally. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilterChips(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelFor: (T) -> String,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        options.forEach { option ->
            TagChip(
                label = labelFor(option),
                selected = option == selected,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
fun AmountRangeSection(
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        OutlinedTextField(
            value = minValue,
            onValueChange = onMinChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Min") },
            prefix = { Text(currencyCode, color = TextSecondary, fontSize = TextSize.sm) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = textFieldColors(),
        )
        OutlinedTextField(
            value = maxValue,
            onValueChange = onMaxChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Max") },
            prefix = { Text(currencyCode, color = TextSecondary, fontSize = TextSize.sm) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = textFieldColors(),
        )
    }
}

@Composable
private fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun TagChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val border = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        SurfaceIconBorder
    }
    val textColor = if (selected) Color.White else TextSecondary

    Row(
        modifier = Modifier
            .background(background, CircleShape)
            .border(0.7.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.large, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = TextSize.sm,
            color = textColor,
        )
    }
}
