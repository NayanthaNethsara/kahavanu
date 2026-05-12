package com.kahavanu.domain.model

data class ExpenseLogEntry(
    val title: String,
    val amount: Double,
    val currency: String,
    val spentAtEpochMillis: Long,
    val merchant: String? = null,
    val category: String,
    val notes: String? = null,
    val paymentMethod: String? = null,
)
