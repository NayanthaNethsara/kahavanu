package com.kahavanu.domain.model

data class ScheduledIncome(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val type: IncomeSourceType,
    val frequency: String? = null,
    val scheduledDateEpochMillis: Long,
    val lastGeneratedEpochMillis: Long? = null,
    val sourceId: Long? = null,
    val sourceName: String? = null,
    val isInvoiceSent: Boolean = false,
    val contactName: String? = null,
    val contactNumber: String? = null,
)
