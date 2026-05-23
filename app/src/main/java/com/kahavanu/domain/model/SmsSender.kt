package com.kahavanu.domain.model

import java.util.Locale

data class SmsSender(
    val id: String,
    val senderName: String,
    val subtitle: String,
    val isEnabled: Boolean,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun resolveSubtitle(name: String): String {
            return when (name.trim().uppercase(Locale.getDefault())) {
                "COMBANK", "COMMERCIAL BANK" -> "Commercial Bank"
                "SAMPATH" -> "Sampath Bank"
                "BOC" -> "Bank of Ceylon"
                "DIALOG" -> "Dialog (mCash)"
                "HNB" -> "Hatton National Bank"
                else -> "SMS Sender Filter"
            }
        }
    }
}
