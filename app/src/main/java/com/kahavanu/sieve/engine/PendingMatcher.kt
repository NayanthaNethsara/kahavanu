package com.kahavanu.sieve.engine

import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.expenses.local.SubscriptionDao
import com.kahavanu.data.income.local.ScheduledIncomeDao
import com.kahavanu.domain.model.SuggestionKind
import javax.inject.Inject
import kotlin.math.abs

/**
 * Result of trying to reconcile a parsed SMS against the user's pending records.
 */
sealed interface PendingMatch {
    /** The SMS settles an open scheduled income with this local id. */
    data class ScheduledIncome(val scheduledIncomeId: Long) : PendingMatch

    /** The SMS is the bank notification for an already auto-logged subscription charge. */
    data class SubscriptionCharge(val subscriptionId: Long) : PendingMatch
}

class PendingMatcher @Inject constructor(
    private val auth: FirebaseAuth,
    private val scheduledIncomeDao: ScheduledIncomeDao,
    private val subscriptionDao: SubscriptionDao,
) {
    /**
     * Attempts to match a parsed SMS to an existing pending record.
     *
     * - INCOME messages are matched to open scheduled incomes (same sender fuzzy, amount
     *   within tolerance, scheduled date within ±7 days).
     * - EXPENSE messages are matched to active subscriptions that were recently charged, so
     *   the bank SMS does not create a duplicate expense alongside the auto-logged charge.
     */
    suspend fun findMatch(parsed: ParsedSms, senderName: String): PendingMatch? {
        val uid = auth.currentUser?.uid ?: return null
        return when (parsed.kind) {
            SuggestionKind.INCOME -> matchScheduledIncome(uid, parsed, senderName)
            SuggestionKind.EXPENSE -> matchSubscription(uid, parsed, senderName)
            else -> null
        }
    }

    private suspend fun matchScheduledIncome(
        uid: String,
        parsed: ParsedSms,
        senderName: String,
    ): PendingMatch? {
        val windowStart = parsed.txnAtEpochMillis - MATCH_WINDOW_MILLIS
        val windowEnd = parsed.txnAtEpochMillis + MATCH_WINDOW_MILLIS
        val candidates = scheduledIncomeDao.getActiveInWindow(uid, windowStart, windowEnd)

        return candidates.firstOrNull { scheduled ->
            val amountClose = amountWithinTolerance(scheduled.amount, parsed.amount)
            val nameMatches = scheduled.sourceName?.let { nameOverlaps(senderName, it) } ?: false
            amountClose && nameMatches
        }?.let { PendingMatch.ScheduledIncome(it.localId) }
    }

    private suspend fun matchSubscription(
        uid: String,
        parsed: ParsedSms,
        senderName: String,
    ): PendingMatch? {
        val candidates = subscriptionDao.getActiveSubscriptions(uid)
        val merchant = parsed.merchant ?: senderName

        return candidates.firstOrNull { sub ->
            if (sub.isPaused) return@firstOrNull false
            val lastCharged = sub.lastGeneratedEpochMillis ?: return@firstOrNull false
            val withinWindow = abs(lastCharged - parsed.txnAtEpochMillis) <= MATCH_WINDOW_MILLIS
            val amountClose = amountWithinTolerance(sub.amount, parsed.amount)
            val nameMatches = nameOverlaps(merchant, sub.title) || nameOverlaps(senderName, sub.title)
            withinWindow && amountClose && nameMatches
        }?.let { PendingMatch.SubscriptionCharge(it.localId) }
    }

    private fun amountWithinTolerance(expected: Double, actual: Double): Boolean {
        if (actual == 0.0) return false
        return abs(expected - actual) / actual < AMOUNT_TOLERANCE
    }

    private fun nameOverlaps(a: String, b: String): Boolean {
        if (a.isBlank() || b.isBlank()) return false
        return a.contains(b, ignoreCase = true) || b.contains(a, ignoreCase = true)
    }

    companion object {
        private const val MATCH_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
        private const val AMOUNT_TOLERANCE = 0.01
    }
}
