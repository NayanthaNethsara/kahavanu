package com.kahavanu.sieve.engine

data class RawSms(
    val senderName: String,
    val body: String,
    val receivedAtEpochMillis: Long,
)
