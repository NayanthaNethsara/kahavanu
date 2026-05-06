package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Difference
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.navigation.AppDestination
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
        BottomNavItem(AppDestination.Home, "Home", Icons.Filled.Home),
        BottomNavItem(AppDestination.Pipeline, "Pipeline", Icons.Outlined.Difference),
        BottomNavItem(AppDestination.Goals, "Goals", Icons.Filled.Settings),
        BottomNavItem(AppDestination.Profile, "Profile", Icons.Outlined.AccountCircle),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = Spacing.large, vertical = Spacing.medium),
    ) {
        // Glass effect background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                )
                .blur(radius = 8.dp),
        )

        // Navigation items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                BottomNavItem(
                    item = item,
                    isActive = currentRoute == item.destination.route,
                    onClick = { onNavigate(item.destination) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.secondary
    } else {
        Color.Transparent
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundColor)
            .clickable(enabled = !isActive) { onClick() }
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            modifier = Modifier.padding(horizontal = Spacing.small),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            if (isActive) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }
        }
    }
}
