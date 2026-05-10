package com.kahavanu.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.income.components.CryptoGatewaySection
import com.kahavanu.ui.income.components.IncomeActionButtons
import com.kahavanu.ui.income.components.IncomeHeader
import com.kahavanu.ui.income.components.IncomeLogSection
import com.kahavanu.ui.income.components.MatchAndCatchSection
import com.kahavanu.ui.income.components.PersistenceSection
import com.kahavanu.ui.income.components.TotalExpectedCard
import com.kahavanu.ui.income.components.currentMonthLabel
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing


@Composable
fun IncomeScreen(
    onLogIncome: () -> Unit,
    onViewPersistence: () -> Unit,
    viewModel: IncomeOverviewViewModel = hiltViewModel(),
) {
    val logs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    val pendingLogs by viewModel.pendingLogs.collectAsStateWithLifecycle()
    val totalIncomeByCurrency by viewModel.totalIncomeByCurrency.collectAsStateWithLifecycle()
    val totalReceivedByCurrency by viewModel.totalReceivedByCurrency.collectAsStateWithLifecycle()
    val breakdownsByCurrency by viewModel.breakdownsByCurrency.collectAsStateWithLifecycle()
    val primaryCurrency by viewModel.primaryCurrency.collectAsStateWithLifecycle()
    val monthLabel = currentMonthLabel()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Emerald.Emerald50.copy(alpha = 0.5f),
                        RawColors.Slate.Slate100
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Spacing.large,
            end = Spacing.large,
            top = 120.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
    ) {
        item { IncomeHeader() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                TotalExpectedCard(
                    totalIncomeByCurrency = totalIncomeByCurrency,
                    totalReceivedByCurrency = totalReceivedByCurrency,
                    primaryCurrency = primaryCurrency,
                    monthLabel = monthLabel,
                    breakdownsByCurrency = breakdownsByCurrency,
                )
                IncomeActionButtons(
                    onLogIncome = onLogIncome,
                    onViewPending = onViewPersistence
                )
            }
        }
        item { MatchAndCatchSection() }
        item { 
            PersistenceSection(
                pendingLogs = pendingLogs,
                onViewAll = onViewPersistence
            ) 
        }
        item { IncomeLogSection(logs = logs) }
    }
}
