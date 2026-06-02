package com.kahavanu.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Central registry of notification channels. Channels are created once at app
 * startup; channel ids are reused by [AppNotifier] when posting notifications.
 */
object NotificationChannels {
    const val GOALS = "goals"
    const val SUBSCRIPTIONS = "subscriptions"
    const val INCOME = "income"
    const val BUDGET = "budget"
    const val SMS = "sms_sieve"

    private data class ChannelSpec(
        val id: String,
        val name: String,
        val description: String,
        val importance: Int,
    )

    private val channels = listOf(
        ChannelSpec(GOALS, "Goals", "Goal milestones and completion", NotificationManager.IMPORTANCE_DEFAULT),
        ChannelSpec(SUBSCRIPTIONS, "Subscriptions & bills", "Recurring charge reminders", NotificationManager.IMPORTANCE_DEFAULT),
        ChannelSpec(INCOME, "Income", "Expected income arrival updates", NotificationManager.IMPORTANCE_DEFAULT),
        ChannelSpec(BUDGET, "Budget", "Overspend and budget alerts", NotificationManager.IMPORTANCE_HIGH),
        ChannelSpec(SMS, "SMS scanning", "Transaction detection from SMS", NotificationManager.IMPORTANCE_LOW),
    )

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        channels.forEach { spec ->
            val channel = NotificationChannel(spec.id, spec.name, spec.importance).apply {
                description = spec.description
            }
            manager.createNotificationChannel(channel)
        }
    }
}
