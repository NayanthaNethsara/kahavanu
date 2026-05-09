package com.kahavanu.ui.income

import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import java.time.LocalDate

enum class CurrencyOption(val code: String, val symbol: String) {
    LKR("LKR", "Rs."),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    AUD("AUD", "A$"),
    JPY("JPY", "¥"),
    INR("INR", "₹"),
    CAD("CAD", "C$"),
}

enum class RecurrenceFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
}

data class IncomeUiState(
    val incomeType: IncomeSourceType = IncomeSourceType.ONE_TIME,
    val sources: List<IncomeSource> = emptyList(),
    val selectedSourceId: Long? = null,
    val clientDescription: String = "",
    val amount: String = "",
    val currency: CurrencyOption = CurrencyOption.LKR,
    val receivedDate: LocalDate? = null,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
