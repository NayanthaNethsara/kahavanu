package com.kahavanu.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kahavanu.MainActivity
import com.kahavanu.R
import com.kahavanu.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

/**
 * Single entry point for posting user-facing notifications. Every notification is
 * gated on the user's "Push alerts" preference and the OS notification permission,
 * so callers can fire freely without re-checking those conditions.
 */
@Singleton
class AppNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {
    private val manager = NotificationManagerCompat.from(context)

    suspend fun notifyGoalCompleted(goalTitle: String) = post(
        channelId = NotificationChannels.GOALS,
        notificationId = stableId("goal", goalTitle),
        title = "Goal reached 🎉",
        text = "You completed “$goalTitle”. Pick your next goal to keep the momentum going.",
    )

    suspend fun notifySubscriptionCharged(name: String, amount: Double, currency: String) = post(
        channelId = NotificationChannels.SUBSCRIPTIONS,
        notificationId = stableId("subscription", name),
        title = "Subscription charged",
        text = "$name · ${formatAmount(amount, currency)} was added to your expenses.",
    )

    suspend fun notifyIncomeArrived(title: String, amount: Double, currency: String) = post(
        channelId = NotificationChannels.INCOME,
        notificationId = stableId("income", title),
        title = "Income recorded",
        text = "$title · ${formatAmount(amount, currency)} has arrived.",
    )

    suspend fun notifyBudgetExceeded(spent: Double, budget: Double, currency: String) = post(
        channelId = NotificationChannels.BUDGET,
        notificationId = BUDGET_NOTIFICATION_ID,
        title = "Over budget",
        text = "You've spent ${formatAmount(spent, currency)} of your ${formatAmount(budget, currency)} monthly budget.",
    )

    private suspend fun post(
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
    ) {
        if (!settingsRepository.observePushAlerts().first()) return
        if (!manager.areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check and the post; ignore.
        }
    }

    private fun formatAmount(amount: Double, currency: String): String {
        val formatted = NumberFormat.getIntegerInstance(Locale.getDefault()).format(amount)
        return "$currency $formatted"
    }

    private fun stableId(prefix: String, key: String): Int =
        (prefix.hashCode() * 31 + key.hashCode()).absoluteValue

    private companion object {
        const val BUDGET_NOTIFICATION_ID = 700_001
    }
}
