package com.kahavanu.ui.income.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.income.HistoryFilter
import com.kahavanu.ui.common.AppSegmentedToggle
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun HistoryFilterToggle(
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    AppSegmentedToggle(
        items = HistoryFilter.entries.toList(),
        selectedItem = selectedFilter,
        onSelect = onFilterSelected,
        labelFor = { filter ->
            filter.name.lowercase().replace("_", " ")
                .replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
        },
        modifier = modifier.fillMaxWidth(),
        height = 40.dp,
        shape = CircleShape,
        containerColor = RawColors.Emerald.Emerald500.copy(alpha = 0.08f),
        indicatorColor = RawColors.Emerald.Emerald600,
        border = BorderStroke(0.5.dp, RawColors.Slate.Slate200.copy(alpha = 0.5f)),
        selectedTextColor = Color.White,
        unselectedTextColor = TextSecondary,
    )
}
