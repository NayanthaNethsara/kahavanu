package com.kahavanu.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.repository.GoalsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalSetupViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalSetupUiState())
    val uiState: StateFlow<GoalSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (primary, _) = settingsRepository.observeCurrencySettings().first()
            _uiState.update { it.copy(currency = primary) }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }

    fun onTargetAmountChange(value: String) {
        if (value.all { c -> c.isDigit() || c == '.' }) {
            _uiState.update { it.copy(targetAmount = value, errorMessage = null) }
        }
    }

    fun onCurrentAmountChange(value: String) {
        if (value.all { c -> c.isDigit() || c == '.' }) {
            _uiState.update { it.copy(currentAmount = value, errorMessage = null) }
        }
    }

    fun onCurrencyChange(currency: CurrencyOption) = _uiState.update { it.copy(currency = currency) }

    fun onCategoryChange(category: GoalCategory) = _uiState.update { it.copy(category = category) }

    fun onDateChange(date: LocalDate) = _uiState.update { it.copy(targetDate = date, isDatePickerOpen = false) }

    fun onDatePickerOpenChange(open: Boolean) = _uiState.update { it.copy(isDatePickerOpen = open) }

    fun saveGoal() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a goal name") }
            return
        }
        val target = state.targetAmount.toDoubleOrNull()
        if (target == null || target <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid target amount") }
            return
        }
        val saved = state.currentAmount.toDoubleOrNull() ?: 0.0
        if (saved < 0.0 || saved > target) {
            _uiState.update { it.copy(errorMessage = "Amount saved cannot exceed the target") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val targetDateMillis = state.targetDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()

            val goal = GoalEntry(
                id = UUID.randomUUID().toString(),
                title = state.title.trim(),
                targetAmount = target,
                currentAmount = saved,
                currency = state.currency,
                category = state.category,
                targetDateEpochMillis = targetDateMillis,
                isCompleted = saved >= target,
                createdAtEpochMillis = System.currentTimeMillis(),
            )
            val result = goalsRepository.addGoal(goal)
            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false, successMessage = "Goal created!") }
            } else {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save goal. Try again.") }
            }
        }
    }
}
