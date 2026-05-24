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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class ManageGoalsViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageGoalsUiState())
    val uiState: StateFlow<ManageGoalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                goalsRepository.observeGoals(),
                settingsRepository.observeCurrencySettings(),
            ) { goals, (primary, _) -> goals to primary }.collect { (goals, primary) ->
                val active = goals.firstOrNull { !it.isCompleted && it.isActive }
                val backlog = goals.filter { !it.isCompleted && it.id != active?.id }
                    .sortedBy { it.priority }
                val completed = goals.filter { it.isCompleted }
                _uiState.update { current ->
                    current.copy(
                        activeGoal = active,
                        backlogGoals = backlog,
                        completedGoals = completed,
                        primaryCurrency = primary,
                        currencyInput = if (current.editingGoalId == null) primary else current.currencyInput,
                    )
                }
            }
        }
    }

    fun openEditSheet(goal: GoalEntry) {
        _uiState.update {
            it.copy(
                editingGoalId = goal.id,
                isSheetOpen = true,
                titleInput = goal.title,
                targetAmountInput = goal.targetAmount.toAmountInput(),
                currentAmountInput = goal.currentAmount.toAmountInput(),
                currencyInput = goal.currency,
                categoryInput = goal.category,
                targetDateInput = goal.targetDateEpochMillis?.let { millis ->
                    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                },
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun closeSheet() {
        _uiState.update {
            it.copy(
                isSheetOpen = false,
                editingGoalId = null,
                isDatePickerOpen = false,
                errorMessage = null,
            )
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(titleInput = value, errorMessage = null)
    }

    fun onTargetAmountChange(value: String) {
        if (value.all { c -> c.isDigit() || c == '.' }) {
            _uiState.update { it.copy(targetAmountInput = value, errorMessage = null) }
        }
    }

    fun onCurrentAmountChange(value: String) {
        if (value.all { c -> c.isDigit() || c == '.' }) {
            _uiState.update { it.copy(currentAmountInput = value, errorMessage = null) }
        }
    }

    fun onCurrencyChange(value: CurrencyOption) = _uiState.update { it.copy(currencyInput = value) }

    fun onCategoryChange(value: GoalCategory) = _uiState.update { it.copy(categoryInput = value) }

    fun onDatePickerOpenChange(open: Boolean) = _uiState.update {
        it.copy(isDatePickerOpen = open)
    }

    fun onDateChange(date: LocalDate) = _uiState.update {
        it.copy(targetDateInput = date, isDatePickerOpen = false)
    }

    fun clearTargetDate() = _uiState.update { it.copy(targetDateInput = null) }

    fun saveEdits() {
        val state = _uiState.value
        val editingId = state.editingGoalId ?: return
        val original = listOfNotNull(state.activeGoal) + state.backlogGoals + state.completedGoals
        val existing = original.firstOrNull { it.id == editingId } ?: return

        val title = state.titleInput.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a goal name") }
            return
        }
        val target = state.targetAmountInput.toDoubleOrNull()
        if (target == null || target <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid target amount") }
            return
        }
        val saved = state.currentAmountInput.toDoubleOrNull() ?: 0.0
        if (saved < 0.0) {
            _uiState.update { it.copy(errorMessage = "Saved amount cannot be negative") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        val targetDateMillis = state.targetDateInput
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()

        val updated = existing.copy(
            title = title,
            targetAmount = target,
            currentAmount = saved,
            currency = state.currencyInput,
            category = state.categoryInput,
            targetDateEpochMillis = targetDateMillis,
            isCompleted = saved >= target,
            lastUpdatedEpochMillis = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            val result = goalsRepository.updateGoal(updated)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSheetOpen = false,
                        editingGoalId = null,
                        successMessage = "Goal updated",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to save",
                    )
                }
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            val result = goalsRepository.deleteGoal(goalId)
            if (result.isSuccess) {
                _uiState.update { it.copy(successMessage = "Goal removed") }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete")
                }
            }
        }
    }

    fun setActiveGoal(goalId: String) {
        viewModelScope.launch {
            val result = goalsRepository.setActiveGoal(goalId)
            if (result.isSuccess) {
                _uiState.update { it.copy(successMessage = "Active goal updated") }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Could not set active goal")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

private fun Double.toAmountInput(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
