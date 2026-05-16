package com.kahavanu.ui.goals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.common.QuickActionButton

@Composable
fun GoalsActionButtons(
    onAddGoal: () -> Unit,
    onViewCompleted: () -> Unit,
    onViewAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickActionButton(
            icon = Icons.Outlined.Add,
            label = "New Goal",
            onClick = onAddGoal,
        )
        QuickActionButton(
            icon = Icons.Outlined.CheckCircle,
            label = "Completed",
            onClick = onViewCompleted,
        )
        QuickActionButton(
            icon = Icons.Outlined.Flag,
            label = "All Goals",
            onClick = onViewAll,
        )
    }
}
