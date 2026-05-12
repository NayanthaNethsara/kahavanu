package com.kahavanu.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.KahavanuShapes
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSecondaryEmerald
import com.kahavanu.ui.theme.TextSize

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = KahavanuShapes.large,
    backgroundColor: Color = Color.White.copy(alpha = 0.85f),
    borderColor: Color = Color.White.copy(alpha = 0.5f),
    shadowElevation: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.shadow(
            elevation = shadowElevation,
            spotColor = RawColors.Gray.Gray400,
            ambientColor = RawColors.Gray.Gray500,
            shape = shape,
        ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(0.5.dp, borderColor),
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    actionText: String? = null,
    badgeCount: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = TextSize.lg,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                if (badgeCount != null) {
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Box(
                        modifier = Modifier
                            .background(RawColors.Emerald.Emerald50, CircleShape)
                            .padding(horizontal = Spacing.medium, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = badgeCount,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = TextSize.xs,
                            color = TextSecondaryEmerald,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                color = TextSecondary,
            )
        }
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                fontSize = TextSize.sm,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier
                    .padding(top = Spacing.extraSmall)
                    .clickable(enabled = onActionClick != null) { onActionClick?.invoke() },
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
