package com.kahavanu.domain.model

enum class NotificationType {
    GOAL,
    SUBSCRIPTION,
    INCOME,
    BUDGET,
    SIEVE,
    GENERAL,
}

data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean,
)
