package com.kahavanu.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.navigation.AppDestination
import com.kahavanu.ui.theme.CornerRadius
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

data class BottomNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
) {
    val items = listOf(
        BottomNavItem(AppDestination.Home, "Home", Icons.Outlined.Home),
        BottomNavItem(AppDestination.Pipeline, "Pipeline", Icons.Outlined.AccountBalanceWallet),
        BottomNavItem(AppDestination.Goals, "Goals", Icons.Outlined.PieChart),
        BottomNavItem(AppDestination.Profile, "Profile", Icons.Outlined.Person),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.large, vertical = Spacing.large)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.full),
            color = Color.White.copy(alpha = 0.85f),
            shadowElevation = Elevation.level4,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = Spacing.small)
            ) {
                val tabWidth = maxWidth / items.size
                val activeIndex = items.indexOfFirst { it.destination.route == currentRoute }.coerceAtLeast(0)

                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * activeIndex,
                    animationSpec = spring(
                        dampingRatio = 0.65f, // A smooth physical bounce
                        stiffness = 400f      // Custom stiffness for a fluid snap
                    ),
                    label = "indicatorOffset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(vertical = Spacing.small, horizontal = Spacing.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(Elevation.level2, RoundedCornerShape(CornerRadius.full))
                            .background(
                                RawColors.Emerald.Emerald500.copy(alpha = 0.85f),
                                RoundedCornerShape(CornerRadius.full)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(CornerRadius.full))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val isActive = currentRoute == item.destination.route
                        BottomNavItem(
                            item = item,
                            isActive = isActive,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(item.destination) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (isActive) Color.White else RawColors.Slate.Slate400,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f
        ),
        label = "contentColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = (-0.2).sp,
                fontSize = 11.sp
            ),
            color = contentColor,
        )
    }
}
