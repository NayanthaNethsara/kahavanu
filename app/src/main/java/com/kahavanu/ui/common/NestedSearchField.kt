package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.SurfaceIconBorder
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

/**
 * Search field with the "inner white box" treatment: an outer translucent
 * pill containing an inner white pill that holds the actual input. Uses
 * [BasicTextField] under the hood so we get tight, predictable vertical
 * sizing (no clipping from Material3 TextField's intrinsic padding).
 */
@Composable
fun NestedSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    val outerShape = RoundedCornerShape(20.dp)
    val innerShape = RoundedCornerShape(16.dp)

    val textStyle = LocalTextStyle.current.merge(
        MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.35f), outerShape)
            .border(0.7.dp, Color.White.copy(alpha = 0.55f), outerShape)
            .padding(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .background(Color.White.copy(alpha = 0.7f), innerShape)
                .border(0.5.dp, SurfaceIconBorder.copy(alpha = 0.5f), innerShape)
                .padding(horizontal = Spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Box(
                modifier = Modifier
                    .padding(start = Spacing.medium)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = TextSecondary.copy(alpha = 0.5f)),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
