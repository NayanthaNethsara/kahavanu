package com.kahavanu.ui.goals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.common.QuickActionButton

@Composable
fun GoalsActionButtons(
    onAddGoal: () -> Unit,
    onViewCompleted: () -> Unit,
    onViewStats: () -> Unit,
    onToggleSort: () -> Unit,
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
            icon = Icons.Outlined.BarChart,
            label = "Stats",
            onClick = onViewStats,
        )
        QuickActionButton(
            icon = Icons.Outlined.Sort,
            label = "Sort",
            onClick = onToggleSort,
        )
    }
}
