package com.kahavanu.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.SurfaceCard
import com.kahavanu.ui.theme.SurfaceIcon
import com.kahavanu.ui.theme.SurfaceIconBorder

/**
 * Round-at-rest icon button that morphs to a 12dp squircle on press with a
 * subtle scale. Used for the back button, filter button, etc. across sub-screens.
 *
 * If [nested] is true, the button renders with an outer tinted card and an
 * inner white surface (concentric "inner white box" look).
 */
@Composable
fun MorphingIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    nested: Boolean = false,
    badge: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (pressed) 12.dp else size / 2,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "MorphingIconButton.cornerRadius",
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "MorphingIconButton.scale",
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clip(shape)
            .background(if (nested) SurfaceCard else SurfaceIcon, shape)
            .border(0.7.dp, SurfaceIconBorder, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (nested) {
            val innerShape = RoundedCornerShape((cornerRadius - 4.dp).coerceAtLeast(0.dp))
            Box(
                modifier = Modifier
                    .size(size - 8.dp)
                    .clip(innerShape)
                    .background(Color.White, innerShape)
                    .border(0.5.dp, SurfaceIconBorder.copy(alpha = 0.6f), innerShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }

        if (badge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Convenience for the standard back button used on sub-screens. */
@Composable
fun MorphingBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MorphingIconButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        onClick = onClick,
        modifier = modifier,
    )
}
