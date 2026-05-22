package com.kahavanu.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isRecurrent
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
    private val incomeRepository: IncomeRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState

    init {
        viewModelScope.launch {
            incomeRepository.ensureDefaultSources()
        }

        viewModelScope.launch {
            incomeRepository.observeIncomeSources().collect { sources ->
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

        viewModelScope.launch {
            settingsRepository.observeCurrencySettings().collect { (primary, secondary) ->
                _uiState.update { current ->
                    val available = listOf(primary, secondary)
                    val newCurrency = if (current.currency !in available) primary else current.currency
                    current.copy(
                        availableCurrencies = available,
                        currency = newCurrency
                    )
                }
            }
        }

    }

    fun onAmountChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        val parts = sanitized.split('.')
        val finalValue = if (parts.size > 2) {
            parts[0] + "." + parts[1]
        } else {
            sanitized
        }
        updateState { it.copy(amount = finalValue) }
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

    fun onFrequencyChange(frequency: RecurrenceFrequency) {
        updateState { it.copy(frequency = frequency) }
    }

    fun onContactSaved(name: String, phoneNumber: String?) {
        updateState { 
            it.copy(
                contactName = name,
                contactNumber = phoneNumber
            ) 
        }
    }

    fun onClearContact() {
        updateState { 
            it.copy(
                contactName = null, 
                contactNumber = null
            ) 
        }
    }

    fun logIncome() {
        viewModelScope.launch {
            val current = _uiState.value
            val clientDescription = current.clientDescription.ifBlank { current.contactName ?: "" }.trim()
            val amountValue = current.amount.trim().toDoubleOrNull()
            val currency = current.currency.code
            val receivedDate = current.receivedDate
            val selectedSource = current.sources.firstOrNull { it.id == current.selectedSourceId }

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

            val result = if (current.incomeType == IncomeSourceType.ONE_TIME) {
                val entry = IncomeLogEntry(
                    title = clientDescription,
                    amount = amountValue,
                    currency = currency,
                    receivedAtEpochMillis = receivedAtEpochMillis,
                    sourceId = current.selectedSourceId,
                    sourceName = selectedSource?.name,
                    sourceType = current.incomeType.id,
                    contactName = current.contactName,
                    contactNumber = current.contactNumber,
                )
                incomeRepository.logIncome(entry).map { 
                    when(it) {
                        IncomeLogResult.SYNCED -> "Income logged"
                        IncomeLogResult.LOCAL_ONLY -> "Saved offline. Will sync when online."
                    }
                }
            } else {
                val scheduled = ScheduledIncome(
                    title = clientDescription,
                    amount = amountValue,
                    currency = currency,
                    type = current.incomeType,
                    frequency = if (current.incomeType == IncomeSourceType.RECURRENT) current.frequency.label else null,
                    scheduledDateEpochMillis = receivedAtEpochMillis,
                    sourceId = current.selectedSourceId,
                    sourceName = selectedSource?.name,
                    contactName = current.contactName,
                    contactNumber = current.contactNumber,
                )
                incomeRepository.upsertScheduledIncome(scheduled).map { "Scheduled income saved" }
            }

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        clientDescription = "",
                        amount = "",
                        currency = current.currency,
                        receivedDate = LocalDate.now(),
                        isSaving = false,
                        successMessage = result.getOrThrow(),
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Could not save income",
                    )
                }
            }
        }
    }

    private fun updateState(transform: (IncomeUiState) -> IncomeUiState) {
        _uiState.update { transform(it).copy(errorMessage = null, successMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
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
