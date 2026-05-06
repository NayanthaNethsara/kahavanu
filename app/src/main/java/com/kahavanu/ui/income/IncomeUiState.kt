package com.kahavanu.ui.income

data class IncomeUiState(
    val title: String = "",
    val amount: String = "",
    val currency: String = "LKR",
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
