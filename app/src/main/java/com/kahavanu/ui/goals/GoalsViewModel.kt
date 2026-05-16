package com.kahavanu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ACTIVE_GOAL_SOFT_LIMIT = 5

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        goalsRepository.observeGoals(),
        settingsRepository.observeCurrencySettings(),
    ) { goals, (primaryCurrency, _) ->
        val active = goals.filter { !it.isCompleted }
        val completed = goals.filter { it.isCompleted }
        GoalsUiState(
            activeGoals = active,
            completedGoals = completed,
            totalTargetAmount = active.sumOf { it.targetAmount },
            totalSavedAmount = active.sumOf { it.currentAmount },
            currency = primaryCurrency,
            activeGoalSoftLimit = ACTIVE_GOAL_SOFT_LIMIT,
            isAtActiveGoalLimit = active.size >= ACTIVE_GOAL_SOFT_LIMIT,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(),
    )

    fun adjustSavedAmount(goalId: String, delta: Double) {
        if (delta == 0.0) return
        viewModelScope.launch {
            goalsRepository.adjustSavedAmount(goalId, delta)
        }
    }
}
