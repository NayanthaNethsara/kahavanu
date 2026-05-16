package com.kahavanu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val sortMode = MutableStateFlow(GoalSortMode.BY_PROGRESS)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalsRepository.observeGoals(),
        settingsRepository.observeCurrencySettings(),
        sortMode,
    ) { goals, (primaryCurrency, _), sort ->
        val active = goals.filter { !it.isCompleted }.sortedWith(sort.comparator())
        val completed = goals.filter { it.isCompleted }
        GoalsUiState(
            activeGoals = active,
            completedGoals = completed,
            totalTargetAmount = active.sumOf { it.targetAmount },
            totalSavedAmount = active.sumOf { it.currentAmount },
            currency = primaryCurrency,
            activeGoalSoftLimit = ACTIVE_GOAL_SOFT_LIMIT,
            isAtActiveGoalLimit = active.size >= ACTIVE_GOAL_SOFT_LIMIT,
            sortMode = sort,
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

    fun toggleSortMode() {
        val modes = GoalSortMode.entries
        val current = sortMode.value
        sortMode.value = modes[(modes.indexOf(current) + 1) % modes.size]
    }
}

private fun GoalSortMode.comparator(): Comparator<com.kahavanu.domain.model.GoalEntry> = when (this) {
    GoalSortMode.BY_PROGRESS -> compareByDescending { g ->
        if (g.targetAmount > 0) g.currentAmount / g.targetAmount else 0.0
    }
    GoalSortMode.BY_REMAINING -> compareBy { g -> g.targetAmount - g.currentAmount }
    GoalSortMode.BY_DATE -> compareBy { g -> g.targetDateEpochMillis ?: Long.MAX_VALUE }
}
