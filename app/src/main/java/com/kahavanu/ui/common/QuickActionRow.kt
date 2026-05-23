package com.kahavanu.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun QuickActionRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        actions.forEach { action ->
            QuickActionButton(
                icon = action.icon,
                label = action.label,
                onClick = action.onClick,
            )
        }
    }
}
