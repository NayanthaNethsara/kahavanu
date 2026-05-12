package com.kahavanu.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun <T> AppSegmentedToggle(
    items: List<T>,
    selectedItem: T,
    onSelect: (T) -> Unit,
    labelFor: (T) -> String,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    shape: Shape = CircleShape,
    containerColor: Color = RawColors.Emerald.Emerald500.copy(alpha = 0.08f),
    indicatorColor: Color = RawColors.Emerald.Emerald600,
    indicatorShadow: Dp = 4.dp,
    indicatorBorder: BorderStroke = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    selectedTextColor: Color = Color.White,
    unselectedTextColor: Color = TextSecondary,
    selectedFontWeight: FontWeight = FontWeight.Bold,
    unselectedFontWeight: FontWeight = FontWeight.Medium,
    border: BorderStroke? = null,
    itemWidth: Dp? = null,
    contentPadding: Dp = 2.dp,
) {
    if (items.isEmpty()) return

    val totalWidth = if (itemWidth != null) itemWidth * items.size else null
    val containerModifier = modifier.then(
        if (totalWidth != null) Modifier.width(totalWidth) else Modifier.fillMaxWidth()
    )

    BoxWithConstraints(modifier = containerModifier) {
        val resolvedItemWidth = itemWidth ?: (maxWidth / items.size)
        val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)
        val indicatorOffset by animateDpAsState(
            targetValue = resolvedItemWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "appSegmentedToggleOffset",
        )

        Surface(
            modifier = Modifier
                .then(if (totalWidth != null) Modifier.width(totalWidth) else Modifier.fillMaxWidth())
                .height(height),
            shape = shape,
            color = containerColor,
            border = border,
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .padding(contentPadding)
                        .size(
                            width = resolvedItemWidth - (contentPadding * 2),
                            height = height - (contentPadding * 2),
                        )
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) },
                    shape = shape,
                    color = indicatorColor,
                    shadowElevation = indicatorShadow,
                    border = indicatorBorder,
                ) {}

                Row(modifier = Modifier.fillMaxSize()) {
                    items.forEach { item ->
                        val isSelected = item == selectedItem
                        Box(
                            modifier = Modifier
                                .width(resolvedItemWidth)
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onSelect(item) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = labelFor(item),
                                style = textStyle,
                                color = if (isSelected) selectedTextColor else unselectedTextColor,
                                fontWeight = if (isSelected) selectedFontWeight else unselectedFontWeight,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
