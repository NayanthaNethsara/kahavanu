package com.kahavanu.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.SurfaceIconBorder
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

data class ActiveFilterChip(val label: String, val onRemove: () -> Unit)

/**
 * Search bar with filter icon button on the same row, plus animated
 * removable chips below when any filters are active.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchWithFiltersBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    filterActive: Boolean,
    activeChips: List<ActiveFilterChip>,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    val filterTint = if (filterActive) MaterialTheme.colorScheme.primary else TextSecondary

    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NestedSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
            )

            MorphingIconButton(
                icon = Icons.Outlined.Tune,
                contentDescription = "Filter",
                onClick = onFilterClick,
                nested = true,
                badge = filterActive,
                tint = filterTint,
                size = 56.dp,
            )
        }

        AnimatedVisibility(
            visible = activeChips.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                activeChips.forEach { chip ->
                    ActiveChip(chip = chip)
                }
            }
        }
    }
}

@Composable
private fun ActiveChip(chip: ActiveFilterChip) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                CircleShape,
            )
            .border(0.7.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
            .clickable(onClick = chip.onRemove)
            .padding(start = Spacing.medium, end = Spacing.small, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = chip.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = TextSize.xs,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "Remove filter",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp),
        )
    }
}
