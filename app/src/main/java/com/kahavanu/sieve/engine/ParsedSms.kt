package com.kahavanu.sieve.engine

import com.kahavanu.domain.model.SuggestionKind

data class ParsedSms(
    val kind: SuggestionKind,
    val amount: Double,
    val currency: String,
    val title: String,
    val merchant: String?,
    val txnAtEpochMillis: Long,
    val confidence: Float,
)
