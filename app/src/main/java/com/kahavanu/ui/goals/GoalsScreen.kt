package com.kahavanu.ui.goals

import androidx.compose.material3.MaterialTheme
import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Sort
import com.kahavanu.ui.common.QuickAction
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.goals.components.ActiveGoalsSection
import com.kahavanu.ui.goals.components.CompletedGoalsSection
import com.kahavanu.ui.goals.components.GoalsSummaryCard
import com.kahavanu.ui.theme.Spacing

@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit = {},
    onViewCompleted: () -> Unit = {},
    onViewStats: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ),
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 140.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.large),
    ) {
        item {
            ScreenHeader(
                label = "Goals",
                title = "What You're Working Toward",
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                GoalsSummaryCard(
                    totalSaved = uiState.totalSavedAmount,
                    totalTarget = uiState.totalTargetAmount,
                    activeCount = uiState.activeGoals.size,
                    completedCount = uiState.completedGoals.size,
                    currency = uiState.currency,
                )
                QuickActionRow(
                    actions = listOf(
                        QuickAction(Icons.Outlined.Add, "New Goal", onAddGoal),
                        QuickAction(Icons.Outlined.CheckCircle, "Completed", onViewCompleted),
                        QuickAction(Icons.Outlined.BarChart, "Stats", onViewStats),
                        QuickAction(Icons.AutoMirrored.Outlined.Sort, "Sort", viewModel::toggleSortMode),
                    ),
                )
            }
        }
        item {
            ActiveGoalsSection(
                goals = uiState.activeGoals,
                currency = uiState.currency,
                softLimit = uiState.activeGoalSoftLimit,
                isAtLimit = uiState.isAtActiveGoalLimit,
                onAddGoal = onAddGoal,
                onAdjustSaved = viewModel::adjustSavedAmount,
            )
        }
        item {
            CompletedGoalsSection(
                goals = uiState.completedGoals,
                currency = uiState.currency,
            )
        }
    }
}
