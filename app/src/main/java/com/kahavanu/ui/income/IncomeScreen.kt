package com.kahavanu.ui.income

import com.kahavanu.ui.util.currentMonthLabel
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
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Repeat
import com.kahavanu.ui.common.QuickAction
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.common.compactAmount
import com.kahavanu.ui.income.components.IncomeLogSection
import com.kahavanu.ui.income.components.MatchAndCatchSection
import com.kahavanu.ui.income.components.PersistenceSection
import com.kahavanu.ui.income.components.TotalExpectedCard
import com.kahavanu.ui.income.components.InsightsSection
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.theme.AccentExpense


@Composable
fun IncomeScreen(
    onLogIncome: () -> Unit,
    onViewRecurrents: () -> Unit,
    onViewPersistence: () -> Unit,
    onViewHistory: () -> Unit,
    viewModel: IncomeOverviewViewModel = hiltViewModel()
) {
    val logs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    val scheduledIncomes by viewModel.scheduledIncomes.collectAsStateWithLifecycle()
    val incomeSuggestions by viewModel.incomeSuggestions.collectAsStateWithLifecycle()
    val totalIncomeByCurrency by viewModel.totalIncomeByCurrency.collectAsStateWithLifecycle()
    val totalReceivedByCurrency by viewModel.totalReceivedByCurrency.collectAsStateWithLifecycle()
    val breakdownsByCurrency by viewModel.breakdownsByCurrency.collectAsStateWithLifecycle()
    val primaryCurrency by viewModel.primaryCurrency.collectAsStateWithLifecycle()
    val thisWeekIncome by viewModel.thisWeekIncome.collectAsStateWithLifecycle()
    val weeklyAverageIncome by viewModel.weeklyAverageIncome.collectAsStateWithLifecycle()
    val incomeTrend by viewModel.incomeTrend.collectAsStateWithLifecycle()
    val topIncomeSource by viewModel.topIncomeSource.collectAsStateWithLifecycle()
    val currentSavings by viewModel.currentSavings.collectAsStateWithLifecycle()
    val monthLabel = currentMonthLabel()

    val pendingScheduled = scheduledIncomes.filter { scheduled ->
        val isPendingOpen = scheduled.type == com.kahavanu.domain.model.IncomeSourceType.PENDING &&
            scheduled.lastGeneratedEpochMillis == null
        val isOverdueRecurrent = scheduled.type == com.kahavanu.domain.model.IncomeSourceType.RECURRENT &&
            com.kahavanu.ui.income.components.isOverdue(scheduled.scheduledDateEpochMillis)
        isPendingOpen || isOverdueRecurrent
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.extendedColors.brandWashed.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 140.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
    ) {
        item { 
            ScreenHeader(
                label = "Income",
                title = "Wealth in the Air"
            ) 
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                TotalExpectedCard(
                    totalIncomeByCurrency = totalIncomeByCurrency,
                    totalReceivedByCurrency = totalReceivedByCurrency,
                    primaryCurrency = primaryCurrency,
                    monthLabel = monthLabel,
                    breakdownsByCurrency = breakdownsByCurrency,
                )
                QuickActionRow(
                    actions = listOf(
                        QuickAction(
                            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            label = "Add",
                            onClick = onLogIncome,
                            iconTint = AccentIncome
                        ),
                        QuickAction(
                            icon = Icons.Outlined.History,
                            label = "All",
                            onClick = onViewHistory,
                            iconTint = MaterialTheme.extendedColors.infoAccent
                        ),
                        QuickAction(
                            icon = Icons.Outlined.Repeat,
                            label = "Recurring",
                            onClick = onViewRecurrents,
                            iconTint = MaterialTheme.extendedColors.accentExpense
                        ),
                        QuickAction(
                            icon = Icons.Outlined.PendingActions,
                            label = "Stats",
                            onClick = onViewPersistence,
                            iconTint = MaterialTheme.extendedColors.warning
                        )
                    ),
                )
            }
        }
        item {
            InsightsSection(
                topSourceTitle = topIncomeSource?.name ?: "—",
                topSourceSubtitle = topIncomeSource?.let {
                    "${compactAmount(primaryCurrency, it.amount.toFloat())} · ${it.percent}%"
                } ?: "No income yet",
                currentSavings = currentSavings,
                thisWeekIncome = thisWeekIncome,
                weeklyAverageIncome = weeklyAverageIncome,
                incomeTrend = incomeTrend,
                currencyCode = primaryCurrency,
            )
        }
        item {
            MatchAndCatchSection(
                items = incomeSuggestions,
                onConfirm = viewModel::confirmIncomeSuggestion,
                onLogAsNew = viewModel::confirmIncomeSuggestion,
                onDismiss = viewModel::dismissIncomeSuggestion,
            )
        }
        item { 
            PersistenceSection(
                scheduledItems = pendingScheduled,
                onViewAll = onViewPersistence,
                onMarkAsReceived = viewModel::markAsReceived
            ) 
        }
        item { 
            IncomeLogSection(
                logs = logs,
                onViewAll = onViewHistory
            ) 
        }
    }
}
