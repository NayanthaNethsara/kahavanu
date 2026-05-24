package com.kahavanu.ui.subscriptions

import com.kahavanu.domain.model.Subscription

import java.time.LocalDate

data class ManageSubscriptionsUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val nameInput: String = "",
    val costInput: String = "",
    val currencyInput: String = "USD",
    val frequencyInput: String = "monthly", // "monthly" or "yearly"
    val nextBillingInput: String = "",
    val nextBillingDate: LocalDate = LocalDate.now(),
    val isDatePickerOpen: Boolean = false,
    val categoryInput: String = "Fun",
    val isSheetOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val totalMonthlySpend: Double
        get() = subscriptions
            .filter { !it.isPaused }
            .sumOf { sub ->
                val cost = sub.cost
                if (sub.frequency.lowercase() == "yearly") {
                    cost / 12.0
                } else {
                    cost
                }
            }
}
