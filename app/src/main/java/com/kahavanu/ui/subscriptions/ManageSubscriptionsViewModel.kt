package com.kahavanu.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kahavanu.domain.model.Subscription
import com.kahavanu.domain.repository.SubscriptionsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ManageSubscriptionsViewModel @Inject constructor(
    private val subscriptionsRepository: SubscriptionsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageSubscriptionsUiState())
    val uiState: StateFlow<ManageSubscriptionsUiState> = _uiState

    init {
        viewModelScope.launch {
            subscriptionsRepository.observeSubscriptions().collect { list ->
                _uiState.update { it.copy(subscriptions = list) }
            }
        }

        viewModelScope.launch {
            settingsRepository.observeCurrencySettings().collect { (primary, _) ->
                _uiState.update { it.copy(currencyInput = primary.code) }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameInput = value, errorMessage = null) }
    }

    fun onCostChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' || it == '-' }
        val parts = sanitized.split('.')
        val finalValue = if (parts.size > 2) {
            parts[0] + "." + parts[1]
        } else {
            sanitized
        }
        _uiState.update { it.copy(costInput = finalValue, errorMessage = null) }
    }

    fun onCurrencyChange(value: String) {
        _uiState.update { it.copy(currencyInput = value) }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { it.copy(frequencyInput = value) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(categoryInput = value) }
    }

    fun onDatePickerOpenChange(isOpen: Boolean) {
        _uiState.update { it.copy(isDatePickerOpen = isOpen) }
    }

    fun onDateChange(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        _uiState.update {
            it.copy(
                nextBillingDate = date,
                nextBillingInput = date.format(formatter),
                isDatePickerOpen = false,
                errorMessage = null
            )
        }
    }

    fun onNextBillingChange(value: String) {
        _uiState.update { it.copy(nextBillingInput = value, errorMessage = null) }
    }

    fun openSheet() {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        val today = LocalDate.now()
        _uiState.update { 
            it.copy(
                isSheetOpen = true,
                nameInput = "",
                costInput = "",
                frequencyInput = "monthly",
                nextBillingDate = today,
                nextBillingInput = today.format(formatter),
                categoryInput = "Fun",
                errorMessage = null,
                successMessage = null
            ) 
        }
    }

    fun closeSheet() {
        _uiState.update { it.copy(isSheetOpen = false) }
    }

    fun toggleSubscriptionPause(id: String) {
        viewModelScope.launch {
            val sub = _uiState.value.subscriptions.firstOrNull { it.id == id } ?: return@launch
            val updatedSub = sub.copy(isPaused = !sub.isPaused)
            subscriptionsRepository.upsertSubscription(updatedSub)
            _uiState.update {
                it.copy(successMessage = "Subscription state toggled")
            }
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            subscriptionsRepository.deleteSubscription(id)
            _uiState.update {
                it.copy(successMessage = "Subscription removed")
            }
        }
    }

    fun addSubscription() {
        val currentState = _uiState.value
        val name = currentState.nameInput.trim()
        val costStr = currentState.costInput.trim()
        val nextBillingDate = currentState.nextBillingDate

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter subscription name") }
            return
        }

        val cost = costStr.toDoubleOrNull()
        if (cost == null || cost <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter a valid positive price") }
            return
        }

        val scheduledDateEpochMillis = nextBillingDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val newSub = Subscription(
            id = UUID.randomUUID().toString(),
            name = name,
            cost = cost,
            currency = currentState.currencyInput,
            frequency = currentState.frequencyInput,
            nextBillingDate = currentState.nextBillingInput,
            isPaused = false,
            category = currentState.categoryInput,
            scheduledDateEpochMillis = scheduledDateEpochMillis,
        )

        viewModelScope.launch {
            val result = subscriptionsRepository.upsertSubscription(newSub)
            if (result.isSuccess) {
                _uiState.update { current ->
                    current.copy(
                        isSheetOpen = false,
                        successMessage = "Subscription added successfully!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to save subscription")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
