package com.kahavanu.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.KahavanuScreen
import com.kahavanu.ui.common.screenSection
import com.kahavanu.ui.common.QuickActionRow
import com.kahavanu.ui.common.QuickAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Flag
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.theme.AccentExpense
import com.kahavanu.ui.theme.InfoAccent
import com.kahavanu.ui.theme.Warning
import com.kahavanu.ui.home.components.OverviewSection
import com.kahavanu.ui.home.components.SieveSection
import com.kahavanu.ui.home.components.StreamsCard
import com.kahavanu.ui.home.components.TreasureCard

@Composable
fun HomeScreen(
    currentSession: UserSession?,
    onGoalClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    onPendingClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.scanNow()
    }

    val onScanClick: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.scanNow()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    KahavanuScreen(
        headerLabel = "Overview",
        headerTitle = "Kahavanu",
        isRefreshing = uiState.isScanning,
        onRefresh = onScanClick,
    ) {
        screenSection {
            TreasureCard(
                activeGoal = uiState.featuredGoal,
                onGoalClick = onGoalClick,
            )
        }
        screenSection {
            OverviewSection(
                totalIncomeThisMonth = uiState.totalIncomeThisMonth,
                totalExpensesThisMonth = uiState.totalExpensesThisMonth,
            )
        }
        screenSection {
            val quickActions = listOf(
                QuickAction(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    label = "Income",
                    onClick = onIncomeClick,
                    iconTint = AccentIncome
                ),
                QuickAction(
                    icon = Icons.AutoMirrored.Outlined.TrendingDown,
                    label = "Expense",
                    onClick = onExpenseClick,
                    iconTint = AccentExpense
                ),
                QuickAction(
                    icon = Icons.Outlined.HourglassEmpty,
                    label = "Pending",
                    onClick = onPendingClick,
                    badgeCount = uiState.sieveItems.size,
                    iconTint = InfoAccent
                ),
                QuickAction(
                    icon = Icons.Outlined.Flag,
                    label = "Goals",
                    onClick = onGoalClick,
                    iconTint = Warning
                )
            )
            QuickActionRow(actions = quickActions)
        }
        if (uiState.isSieveEnabled) {
            item {
                SieveSection(
                    items = uiState.sieveItems,
                    onConfirm = viewModel::confirmSieveItem,
                    onIgnore = viewModel::dismissSieveItem,
                    onScanClick = onScanClick,
                    isScanning = uiState.isScanning,
                )
            }
        }
        screenSection {
            StreamsCard(
                incomeStreams = uiState.incomeStreams,
                onIncomeClick = onIncomeClick,
            )
        }
    }
}
