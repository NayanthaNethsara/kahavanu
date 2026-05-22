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
import com.kahavanu.ui.home.components.SieveSection
import com.kahavanu.ui.home.components.StreamsCard
import com.kahavanu.ui.home.components.TreasureCard

@Composable
fun HomeScreen(
    currentSession: UserSession?,
    onGoalClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
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
        item {
            SieveSection(
                items = uiState.sieveItems,
                onConfirm = viewModel::confirmSieveItem,
                onIgnore = viewModel::dismissSieveItem,
                onScanClick = onScanClick,
                isScanning = uiState.isScanning,
            )
        }
        screenSection {
            StreamsCard(
                incomeStreams = uiState.incomeStreams,
                onIncomeClick = onIncomeClick,
            )
        }
    }
}
