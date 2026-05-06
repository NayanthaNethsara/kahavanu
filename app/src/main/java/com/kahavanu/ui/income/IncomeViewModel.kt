package com.kahavanu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val repository: IncomeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState

    fun onTitleChange(value: String) {
        updateState { it.copy(title = value) }
    }

    fun onAmountChange(value: String) {
        updateState { it.copy(amount = value) }
    }

    fun onCurrencyChange(value: String) {
        updateState { it.copy(currency = value) }
    }

    fun onNoteChange(value: String) {
        updateState { it.copy(note = value) }
    }

    fun logIncome() {
        viewModelScope.launch {
            val current = _uiState.value
            val title = current.title.trim()
            val amountValue = current.amount.trim().toDoubleOrNull()
            val currency = current.currency.trim().ifBlank { "LKR" }.uppercase()

            if (title.isBlank() || amountValue == null || amountValue <= 0.0) {
                _uiState.update {
                    it.copy(errorMessage = "Enter a title and valid amount")
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            val entry = IncomeLogEntry(
                title = title,
                amount = amountValue,
                currency = currency,
                note = current.note.trim().ifBlank { null },
                receivedAtEpochMillis = System.currentTimeMillis(),
            )

            val result = repository.logIncome(entry)
            _uiState.update {
                if (result.isSuccess) {
                    val successMessage = when (result.getOrThrow()) {
                        IncomeLogResult.SYNCED -> "Income logged"
                        IncomeLogResult.LOCAL_ONLY -> "Saved offline. Will sync when online."
                    }
                    it.copy(
                        title = "",
                        amount = "",
                        note = "",
                        currency = currency,
                        isSaving = false,
                        successMessage = successMessage,
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Could not log income",
                    )
                }
            }
        }
    }

    private fun updateState(transform: (IncomeUiState) -> IncomeUiState) {
        _uiState.update { transform(it).copy(errorMessage = null, successMessage = null) }
    }
}
