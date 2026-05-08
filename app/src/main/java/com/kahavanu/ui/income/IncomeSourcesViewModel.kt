package com.kahavanu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeSourcesViewModel @Inject constructor(
    private val repository: IncomeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeSourcesUiState())
    val uiState: StateFlow<IncomeSourcesUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.ensureDefaultSources()
        }

        viewModelScope.launch {
            repository.observeIncomeSources().collect { sources ->
                _uiState.update { current -> current.copy(sources = sources) }
            }
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

    fun startEdit(source: IncomeSource) {
        updateState {
            it.copy(
                editingSourceId = source.id,
                nameInput = source.name,
                selectedTypes = source.types,
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
            val result = repository.upsertIncomeSource(source)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        nameInput = "",
                        selectedTypes = IncomeSourceType.values().toSet(),
                        editingSourceId = null,
                        isSaving = false,
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
            val result = repository.deleteIncomeSource(source.id)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Could not delete")
                }
            }
        }
    }

    private fun updateState(transform: (IncomeSourcesUiState) -> IncomeSourcesUiState) {
        _uiState.update { transform(it).copy(errorMessage = null, successMessage = null) }
    }
}
