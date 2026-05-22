package com.kahavanu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalAdjustmentLog
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompletedGoalsUiState(
    val goals: List<GoalEntry> = emptyList(),
    val currency: CurrencyOption = CurrencyOption.LKR,
)

@HiltViewModel
class CompletedGoalsViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<CompletedGoalsUiState> = combine(
        goalsRepository.observeGoals(),
        settingsRepository.observeCurrencySettings(),
    ) { goals, (primaryCurrency, _) ->
        CompletedGoalsUiState(
            goals = goals.filter { it.isCompleted }.sortedByDescending { it.lastUpdatedEpochMillis },
            currency = primaryCurrency,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompletedGoalsUiState(),
    )

    fun observeLogsForGoal(goalClientId: String): Flow<List<GoalAdjustmentLog>> =
        goalsRepository.observeAdjustmentLogs(goalClientId)
}
