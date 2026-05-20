package com.kahavanu.sieve.engine

data class SmsRule(
    val name: String,
    val senderPattern: Regex,
    val bodyPattern: Regex,
    val parse: (senderName: String, body: String, receivedAtEpochMillis: Long) -> ParsedSms?,
)
