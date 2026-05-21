package com.kahavanu.domain.model

data class SmsSuggestion(
    val localId: Long,
    val userId: String,
    val smsSenderName: String,
    val smsBodyHash: String,
    val smsReceivedAtEpochMillis: Long,
    val kind: SuggestionKind,
    val amount: Double,
    val currency: String,
    val title: String,
    val merchant: String?,
    val txnAtEpochMillis: Long,
    val matchedScheduledIncomeId: Long?,
    val status: SuggestionStatus,
    val confidence: Float,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
