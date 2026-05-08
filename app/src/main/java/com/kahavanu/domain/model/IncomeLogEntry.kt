package com.kahavanu.domain.model

data class IncomeLogEntry(
    val title: String,
    val amount: Double,
    val currency: String,
    val note: String?,
    val receivedAtEpochMillis: Long,
)
