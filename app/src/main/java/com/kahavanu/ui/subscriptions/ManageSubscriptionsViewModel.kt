package com.kahavanu.ui.subscriptions

import androidx.lifecycle.ViewModel
import com.kahavanu.domain.model.Subscription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ManageSubscriptionsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        ManageSubscriptionsUiState(
            subscriptions = listOf(
                Subscription(
                    id = "netflix",
                    name = "Netflix Standard",
                    cost = 15.49,
                    currency = "USD",
                    frequency = "monthly",
                    nextBillingDate = "June 5, 2026",
                    isPaused = false
                ),
                Subscription(
                    id = "spotify",
                    name = "Spotify Premium",
                    cost = 10.99,
                    currency = "USD",
                    frequency = "monthly",
                    nextBillingDate = "June 12, 2026",
                    isPaused = false
                ),
                Subscription(
                    id = "chatgpt",
                    name = "ChatGPT Plus",
                    cost = 20.00,
                    currency = "USD",
                    frequency = "monthly",
                    nextBillingDate = "June 20, 2026",
                    isPaused = false
                ),
                Subscription(
                    id = "github",
                    name = "GitHub Copilot",
                    cost = 10.00,
                    currency = "USD",
                    frequency = "monthly",
                    nextBillingDate = "June 25, 2026",
                    isPaused = true
                ),
                Subscription(
                    id = "googleone",
                    name = "Google One 100GB",
                    cost = 1.99,
                    currency = "USD",
                    frequency = "monthly",
                    nextBillingDate = "June 8, 2026",
                    isPaused = false
                )
            )
        )
    )
    val uiState: StateFlow<ManageSubscriptionsUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.update { it.copy(nameInput = value, errorMessage = null) }
    }

    fun onCostChange(value: String) {
        _uiState.update { it.copy(costInput = value, errorMessage = null) }
    }

    fun onCurrencyChange(value: String) {
        _uiState.update { it.copy(currencyInput = value) }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { it.copy(frequencyInput = value) }
    }

    fun onNextBillingChange(value: String) {
        _uiState.update { it.copy(nextBillingInput = value, errorMessage = null) }
    }

    fun openSheet() {
        _uiState.update { 
            it.copy(
                isSheetOpen = true,
                nameInput = "",
                costInput = "",
                currencyInput = "USD",
                frequencyInput = "monthly",
                nextBillingInput = "",
                errorMessage = null,
                successMessage = null
            ) 
        }
    }

    fun closeSheet() {
        _uiState.update { it.copy(isSheetOpen = false) }
    }

    fun toggleSubscriptionPause(id: String) {
        _uiState.update { current ->
            val updated = current.subscriptions.map { sub ->
                if (sub.id == id) {
                    sub.copy(isPaused = !sub.isPaused)
                } else {
                    sub
                }
            }
            current.copy(
                subscriptions = updated,
                successMessage = "Subscription state toggled"
            )
        }
    }

    fun deleteSubscription(id: String) {
        _uiState.update { current ->
            val updated = current.subscriptions.filter { it.id != id }
            current.copy(
                subscriptions = updated,
                successMessage = "Subscription removed"
            )
        }
    }

    fun addSubscription() {
        val currentState = _uiState.value
        val name = currentState.nameInput.trim()
        val costStr = currentState.costInput.trim()
        val nextBilling = currentState.nextBillingInput.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter subscription name") }
            return
        }

        val cost = costStr.toDoubleOrNull()
        if (cost == null || cost <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Enter a valid positive price") }
            return
        }

        if (nextBilling.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter next billing date") }
            return
        }

        val newSub = Subscription(
            id = UUID.randomUUID().toString(),
            name = name,
            cost = cost,
            currency = currentState.currencyInput,
            frequency = currentState.frequencyInput,
            nextBillingDate = nextBilling,
            isPaused = false
        )

        _uiState.update { current ->
            current.copy(
                subscriptions = current.subscriptions + newSub,
                isSheetOpen = false,
                successMessage = "Subscription added successfully!"
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
