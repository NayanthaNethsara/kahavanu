package com.kahavanu.ui.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeSourcesViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeSourcesUiState())
    val uiState: StateFlow<IncomeSourcesUiState> = _uiState

    init {
        viewModelScope.launch {
            incomeRepository.ensureDefaultSources()
        }

        viewModelScope.launch {
            incomeRepository.observeIncomeSources().collect { sources ->
                _uiState.update { current -> current.copy(sources = sources) }
            }
        }

        viewModelScope.launch {
            settingsRepository.observeCurrencySettings().collect { (primary, secondary) ->
                _uiState.update { 
                    it.copy(
                        primaryCurrency = primary, 
                        secondaryCurrency = secondary,
                        primaryCurrencyDraft = primary,
                        secondaryCurrencyDraft = secondary
                    ) 
                }
            }
        }
    }

    fun startCurrencyEdit() {
        updateState { 
            it.copy(
                isCurrencyEditing = true,
                primaryCurrencyDraft = it.primaryCurrency,
                secondaryCurrencyDraft = it.secondaryCurrency
            ) 
        }
    }

    fun cancelCurrencyEdit() {
        updateState { it.copy(isCurrencyEditing = false) }
    }

    fun onPrimaryCurrencyDraftChange(currency: CurrencyOption) {
        updateState { it.copy(primaryCurrencyDraft = currency) }
    }

    fun onSecondaryCurrencyDraftChange(currency: CurrencyOption) {
        updateState { it.copy(secondaryCurrencyDraft = currency) }
    }

    fun saveCurrencySettings() {
        viewModelScope.launch {
            val primary = _uiState.value.primaryCurrencyDraft
            val secondary = _uiState.value.secondaryCurrencyDraft
            settingsRepository.updateCurrencySettings(primary, secondary)
            updateState { it.copy(isCurrencyEditing = false) }
        }
    }

    fun onNameChange(value: String) {
        updateState { it.copy(nameInput = value) }
    }

    fun onTypeToggle(type: IncomeSourceType) {
        updateState { current ->
            val updated = current.selectedTypes.toMutableSet()
            if (updated.contains(type)) {
                updated.remove(type)
            } else {
                updated.add(type)
            }
            current.copy(selectedTypes = updated)
        }
    }

    fun openSheet() {
        updateState { it.copy(isSheetOpen = true) }
    }

    fun closeSheet() {
        cancelEdit()
        updateState { it.copy(isSheetOpen = false) }
    }

    fun showDeleteConfirmation(source: IncomeSource) {
        updateState { it.copy(sourceToDelete = source) }
    }

    fun dismissDeleteConfirmation() {
        updateState { it.copy(sourceToDelete = null) }
    }

    fun startEdit(source: IncomeSource) {
        updateState {
            it.copy(
                editingSourceId = source.id,
                nameInput = source.name,
                selectedTypes = source.types,
                isSheetOpen = true,
            )
        }
    }

    fun cancelEdit() {
        updateState {
            it.copy(
                editingSourceId = null,
                nameInput = "",
                selectedTypes = IncomeSourceType.values().toSet(),
            )
        }
    }

    fun saveSource() {
        viewModelScope.launch {
            val current = _uiState.value
            val name = current.nameInput.trim()
            if (name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Enter a source name") }
                return@launch
            }
            if (current.selectedTypes.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Select at least one type") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            val source = IncomeSource(
                id = current.editingSourceId ?: 0L,
                name = name,
                types = current.selectedTypes,
            )
            val result = incomeRepository.upsertIncomeSource(source)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        nameInput = "",
                        selectedTypes = IncomeSourceType.values().toSet(),
                        editingSourceId = null,
                        isSaving = false,
                        isSheetOpen = false,
                        successMessage = if (current.editingSourceId == null) {
                            "Source added"
                        } else {
                            "Source updated"
                        },
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Could not save source",
                    )
                }
            }
        }
    }

    fun deleteSource(source: IncomeSource) {
        viewModelScope.launch {
            val result = incomeRepository.deleteIncomeSource(source.id)
            _uiState.update { current ->
                if (result.isFailure) {
                    current.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Could not delete",
                        sourceToDelete = null
                    )
                } else {
                    current.copy(sourceToDelete = null)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun updateState(transform: (IncomeSourcesUiState) -> IncomeSourcesUiState) {
        _uiState.update { transform(it).copy(errorMessage = null, successMessage = null) }
    }
}
