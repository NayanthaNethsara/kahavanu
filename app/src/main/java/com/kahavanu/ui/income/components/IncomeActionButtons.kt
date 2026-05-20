package com.kahavanu.ui.income.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kahavanu.ui.common.QuickActionButton
import com.kahavanu.ui.theme.Spacing

@Composable
fun IncomeActionButtons(
    onLogIncome: () -> Unit,
    onViewRecurrents: () -> Unit,
    onViewPending: () -> Unit,
    onViewHistory: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
            label = "Log Income",
            onClick = onLogIncome
        )
        QuickActionButton(
            icon = Icons.Outlined.Repeat,
            label = "View Recurrents",
            onClick = onViewRecurrents
        )
        QuickActionButton(
            icon = Icons.Outlined.PendingActions,
            label = "Pending",
            onClick = onViewPending
        )
        QuickActionButton(
            icon = Icons.Outlined.History,
            label = "History",
            onClick = onViewHistory
        )
    }
}
