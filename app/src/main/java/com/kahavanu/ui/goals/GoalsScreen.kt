package com.kahavanu.ui.goals

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
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.goals.components.ActiveGoalsSection
import com.kahavanu.ui.goals.components.CompletedGoalsSection
import com.kahavanu.ui.goals.components.GoalsActionButtons
import com.kahavanu.ui.goals.components.GoalsSummaryCard
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Emerald.Emerald50.copy(alpha = 0.4f),
                        RawColors.Slate.Slate100,
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
                GoalsActionButtons(
                    onAddGoal = onAddGoal,
                    onViewCompleted = {},
                    onViewAll = {},
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
