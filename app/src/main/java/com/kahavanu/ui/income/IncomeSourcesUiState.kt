package com.kahavanu.ui.income

import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType

data class IncomeSourcesUiState(
    val sources: List<IncomeSource> = emptyList(),
    val nameInput: String = "",
    val selectedTypes: Set<IncomeSourceType> = IncomeSourceType.values().toSet(),
    val editingSourceId: Long? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
