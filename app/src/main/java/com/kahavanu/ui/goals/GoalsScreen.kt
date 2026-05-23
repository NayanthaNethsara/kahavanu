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
import androidx.compose.ui.graphics.Color
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
import com.kahavanu.ui.goals.components.BacklogSection
import com.kahavanu.ui.goals.components.CompletedGoalsSection
import com.kahavanu.ui.goals.components.GoalsSummaryCard
import com.kahavanu.ui.goals.components.ProjectionsSection
import com.kahavanu.ui.goals.components.TradeOffSimulator
import com.kahavanu.ui.theme.Spacing

@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit = {},
    onViewCompleted: () -> Unit = {},
    onViewStats: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val featuredGoal = uiState.activeGoals.firstOrNull()

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
                // Featured Active Goal Card
                GoalsSummaryCard(
                    featuredGoal = featuredGoal,
                    currency = uiState.currency,
                )
                
                // Color-coded Quick Actions
                QuickActionRow(
                    actions = listOf(
                        QuickAction(
                            icon = Icons.Outlined.Add,
                            label = "New Goal",
                            onClick = onAddGoal,
                            iconTint = Color(0xFF00BC7D) // Emerald
                        ),
                        QuickAction(
                            icon = Icons.Outlined.CheckCircle,
                            label = "Completed",
                            onClick = onViewCompleted,
                            iconTint = Color(0xFF3B82F6) // Blue
                        ),
                        QuickAction(
                            icon = Icons.Outlined.BarChart,
                            label = "Stats",
                            onClick = onViewStats,
                            iconTint = Color(0xFF8B5CF6) // Violet
                        ),
                        QuickAction(
                            icon = Icons.AutoMirrored.Outlined.Sort,
                            label = "Sort",
                            onClick = viewModel::toggleSortMode,
                            iconTint = Color(0xFFF97316) // Orange
                        ),
                    ),
                )
            }
        }
        
        // Estimated Arrival & Capacity Projections
        item {
            val remaining = featuredGoal?.let { it.targetAmount - it.currentAmount } ?: 478800.0
            ProjectionsSection(
                remainingAmount = remaining,
                rcsAmount = 53000.0,
                currency = uiState.currency,
            )
        }

        // Live Interactive Trade-Off Simulator
        item {
            TradeOffSimulator(
                featuredGoal = featuredGoal,
                currency = uiState.currency,
            )
        }

        // Remaining active backlog goals
        item {
            BacklogSection(
                goals = uiState.activeGoals.drop(1),
                currency = uiState.currency,
                onAdjustSaved = viewModel::adjustSavedAmount,
                onAddGoal = onAddGoal,
            )
        }

        // Achieved completed goals
        item {
            CompletedGoalsSection(
                goals = uiState.completedGoals,
                currency = uiState.currency,
            )
        }
    }
}
