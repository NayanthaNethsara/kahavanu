package com.kahavanu.ui.income

import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import java.time.LocalDate

enum class CurrencyOption(val code: String) {
    LKR("LKR"),
    USD("USD"),
}

data class IncomeUiState(
    val incomeType: IncomeSourceType = IncomeSourceType.ONE_TIME,
    val sources: List<IncomeSource> = emptyList(),
    val selectedSourceId: Long? = null,
    val clientDescription: String = "",
    val amount: String = "",
    val currency: CurrencyOption = CurrencyOption.LKR,
    val receivedDate: LocalDate? = null,
    val isDatePickerOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
