package com.kahavanu.ui.income.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.income.PersistenceFilter
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PersistenceFilterToggle(
    selectedFilter: PersistenceFilter,
    onFilterSelected: (PersistenceFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / PersistenceFilter.entries.size
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedFilter.ordinal,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "filterIndicatorOffset"
        )

        Surface(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = CircleShape,
            color = RawColors.Emerald.Emerald500.copy(alpha = 0.08f),
            border = BorderStroke(0.5.dp, RawColors.Slate.Slate200.copy(alpha = 0.5f))
        ) {
            Box {
                // Animated Pill
                Surface(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(width = tabWidth - 4.dp, height = 36.dp)
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) },
                    shape = CircleShape,
                    color = RawColors.Emerald.Emerald600,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {}

                // Labels
                Row(modifier = Modifier.fillMaxSize()) {
                    PersistenceFilter.entries.forEach { filter ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onFilterSelected(filter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.name.lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedFilter == filter) Color.White else TextSecondary,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
