package com.kahavanu.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.AmbientGlow
import com.kahavanu.ui.common.ScreenHeader
import com.kahavanu.ui.home.components.SieveSection
import com.kahavanu.ui.home.components.StreamsCard
import com.kahavanu.ui.home.components.TreasureCard
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun HomeScreen(
    currentSession: UserSession?,
    onGoalClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Decorative background glowing ambient circles matching Figma coordinates
        AmbientGlow(
            color = RawColors.Emerald.Emerald400.copy(alpha = 0.18f),
            size = 360.dp,
            modifier = Modifier.offset(x = (-96).dp, y = (-128).dp)
        )

        AmbientGlow(
            color = RawColors.Emerald.Emerald400.copy(alpha = 0.12f),
            size = 320.dp,
            modifier = Modifier.offset(x = 170.dp, y = 284.dp)
        )

        AmbientGlow(
            color = RawColors.Slate.Slate900.copy(alpha = 0.06f),
            size = 300.dp,
            modifier = Modifier.offset(x = 98.dp, y = 648.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 140.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.extraLarge)
                ) {
                    ScreenHeader(
                        label = "Overview",
                        title = "Kahavanu"
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.extraLarge)
                ) {
                    TreasureCard(
                        activeGoal = uiState.featuredGoal,
                        onGoalClick = onGoalClick
                    )
                }
            }

            if (uiState.sieveItems.isNotEmpty()) {
                item {
                    SieveSection(
                        items = uiState.sieveItems,
                        onConfirm = viewModel::confirmSieveItem,
                        onIgnore = viewModel::dismissSieveItem
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.extraLarge)
                ) {
                    StreamsCard(
                        incomeStreams = uiState.incomeStreams,
                        onIncomeClick = onIncomeClick
                    )
                }
            }
        }
    }
}
