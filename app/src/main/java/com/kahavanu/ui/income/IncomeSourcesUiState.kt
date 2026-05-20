package com.kahavanu.ui.income

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType

data class IncomeSourcesUiState(
    val sources: List<IncomeSource> = emptyList(),
    val nameInput: String = "",
    val selectedTypes: Set<IncomeSourceType> = IncomeSourceType.values().toSet(),
    val editingSourceId: Long? = null,
    val isSaving: Boolean = false,
    val isSheetOpen: Boolean = false,
    val sourceToDelete: IncomeSource? = null,
    val primaryCurrency: CurrencyOption = CurrencyOption.LKR,
    val secondaryCurrency: CurrencyOption = CurrencyOption.USD,
    val primaryCurrencyDraft: CurrencyOption = CurrencyOption.LKR,
    val secondaryCurrencyDraft: CurrencyOption = CurrencyOption.USD,
    val isCurrencyEditing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
