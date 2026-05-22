package com.kahavanu.ui.income.components

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSize

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
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
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
                if (selected) MaterialTheme.extendedColors.brandAccent else Color.Transparent,
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
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), KahavanuShapes.medium)
                .clickable { expanded = true }
                .padding(horizontal = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected.code,
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
                            text = option.code,
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
