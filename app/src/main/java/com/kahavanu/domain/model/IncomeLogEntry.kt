package com.kahavanu.domain.model

data class IncomeLogEntry(
    val title: String,
    val amount: Double,
    val currency: String,
    val receivedAtEpochMillis: Long,
    val sourceId: Long? = null,
    val sourceName: String? = null,
    val sourceType: String? = null,
    val isInvoiceSent: Boolean = false,
    val frequency: String? = null,
    val contactName: String? = null,
    val contactNumber: String? = null,
)
