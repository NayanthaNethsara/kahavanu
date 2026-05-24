package com.kahavanu.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.QuickAction
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.goals.components.BacklogSection
import com.kahavanu.ui.goals.components.CompletedGoalsSection
import com.kahavanu.ui.goals.components.GoalsSummaryCard
import com.kahavanu.ui.goals.components.ProjectionsSection
import com.kahavanu.ui.goals.components.TradeOffSimulator
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.extendedColors

@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit = {},
    onViewCompleted: () -> Unit = {},
    onViewStats: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeGoal = uiState.activeGoal
    val remaining = activeGoal?.let { (it.targetAmount - it.currentAmount).coerceAtLeast(0.0) } ?: 0.0

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
                    featuredGoal = activeGoal,
                    currency = uiState.currency,
                )

                QuickActionRow(
                    actions = listOf(
                        QuickAction(
                            icon = Icons.Outlined.Add,
                            label = "New Goal",
                            onClick = onAddGoal,
                            iconTint = Color(0xFF00BC7D),
                        ),
                        QuickAction(
                            icon = Icons.Outlined.CheckCircle,
                            label = "Completed",
                            onClick = onViewCompleted,
                            iconTint = Color(0xFF3B82F6),
                        ),
                        QuickAction(
                            icon = Icons.Outlined.BarChart,
                            label = "Stats",
                            onClick = onViewStats,
                            iconTint = Color(0xFF8B5CF6),
                        ),
                        QuickAction(
                            icon = Icons.AutoMirrored.Outlined.Sort,
                            label = "Sort",
                            onClick = viewModel::toggleSortMode,
                            iconTint = Color(0xFFF97316),
                        ),
                    ),
                )
            }
        }

        item {
            ProjectionsSection(
                remainingAmount = remaining,
                capacity = uiState.capacity,
                currency = uiState.currency,
                activeGoalTitle = activeGoal?.title,
            )
        }

        item {
            TradeOffSimulator(
                featuredGoal = activeGoal,
                currency = uiState.currency,
            )
        }

        item {
            BacklogSection(
                goals = uiState.backlogGoals,
                currency = uiState.currency,
                onAdjustSaved = viewModel::adjustSavedAmount,
                onAddGoal = onAddGoal,
                onMakeActive = viewModel::setActiveGoal,
                onReorder = viewModel::reorderBacklog,
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
