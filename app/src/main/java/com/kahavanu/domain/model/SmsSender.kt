package com.kahavanu.domain.model

data class SmsSender(
    val id: String,
    val senderName: String,
    val isEnabled: Boolean,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
