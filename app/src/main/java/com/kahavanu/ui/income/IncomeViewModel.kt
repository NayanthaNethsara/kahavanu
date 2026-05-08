package com.kahavanu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val repository: IncomeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.ensureDefaultSources()
        }

        viewModelScope.launch {
            repository.observeIncomeSources().collect { sources ->
                _uiState.update { current ->
                    val selectedSourceId = resolveSelectedSourceId(
                        sources = sources,
                        preferredSourceId = current.selectedSourceId,
                        incomeType = current.incomeType,
                    )
                    current.copy(
                        sources = sources,
                        selectedSourceId = selectedSourceId,
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        updateState { it.copy(amount = value) }
    }

    fun onClientDescriptionChange(value: String) {
        updateState { it.copy(clientDescription = value) }
    }

    fun onIncomeTypeChange(type: IncomeSourceType) {
        updateState { current ->
            val selectedSourceId = resolveSelectedSourceId(
                sources = current.sources,
                preferredSourceId = current.selectedSourceId,
                incomeType = type,
            )
            current.copy(incomeType = type, selectedSourceId = selectedSourceId)
        }
    }

    fun onSourceChange(sourceId: Long) {
        updateState { it.copy(selectedSourceId = sourceId) }
    }

    fun onCurrencyChange(currency: CurrencyOption) {
        updateState { it.copy(currency = currency) }
    }

    fun onDatePickerOpenChange(isOpen: Boolean) {
        updateState { it.copy(isDatePickerOpen = isOpen) }
    }

    fun onDateChange(date: LocalDate) {
        updateState { it.copy(receivedDate = date, isDatePickerOpen = false) }
    }

    fun logIncome() {
        viewModelScope.launch {
            val current = _uiState.value
            val clientDescription = current.clientDescription.trim()
            val amountValue = current.amount.trim().toDoubleOrNull()
            val currency = current.currency.code
            val receivedDate = current.receivedDate ?: LocalDate.now()
            val selectedSource = current.sources.firstOrNull { it.id == current.selectedSourceId }
            val note = "${current.incomeType.label} - ${selectedSource?.name ?: "Source"}"

            if (clientDescription.isBlank() || amountValue == null || amountValue <= 0.0) {
                _uiState.update {
                    it.copy(errorMessage = "Enter a client/description and valid amount")
                }
                return@launch
            }

            if (selectedSource == null) {
                _uiState.update { it.copy(errorMessage = "Select an income source") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            val receivedAtEpochMillis = receivedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val entry = IncomeLogEntry(
                title = clientDescription,
                amount = amountValue,
                currency = currency,
                note = note,
                receivedAtEpochMillis = receivedAtEpochMillis,
            )

            val result = repository.logIncome(entry)
            _uiState.update {
                if (result.isSuccess) {
                    val successMessage = when (result.getOrThrow()) {
                        IncomeLogResult.SYNCED -> "Income logged"
                        IncomeLogResult.LOCAL_ONLY -> "Saved offline. Will sync when online."
                    }
                    it.copy(
                        clientDescription = "",
                        amount = "",
                        currency = current.currency,
                        receivedDate = null,
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

    private fun resolveSelectedSourceId(
        sources: List<IncomeSource>,
        preferredSourceId: Long?,
        incomeType: IncomeSourceType,
    ): Long? {
        val preferred = preferredSourceId?.let { id ->
            sources.firstOrNull { it.id == id && it.types.contains(incomeType) }
        }
        return preferred?.id ?: sources.firstOrNull { it.types.contains(incomeType) }?.id
    }
}
