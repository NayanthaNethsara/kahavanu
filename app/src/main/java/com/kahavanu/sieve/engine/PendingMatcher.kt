package com.kahavanu.sieve.engine

import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.income.local.ScheduledIncomeDao
import com.kahavanu.domain.model.SuggestionKind
import javax.inject.Inject
import kotlin.math.abs

class PendingMatcher @Inject constructor(
    private val auth: FirebaseAuth,
    private val scheduledIncomeDao: ScheduledIncomeDao,
) {
    /**
     * Returns the localId of a matching open ScheduledIncome, or null if none found.
     * Match criteria: same sender (fuzzy), amount within 1%, scheduled date within ±7 days.
     */
    suspend fun findMatch(parsed: ParsedSms, senderName: String): Long? {
        if (parsed.kind != SuggestionKind.INCOME) return null
        val uid = auth.currentUser?.uid ?: return null

        val windowStart = parsed.txnAtEpochMillis - MATCH_WINDOW_MILLIS
        val windowEnd = parsed.txnAtEpochMillis + MATCH_WINDOW_MILLIS

        val candidates = scheduledIncomeDao.getActiveInWindow(uid, windowStart, windowEnd)

        return candidates.firstOrNull { scheduled ->
            val amountClose = abs(scheduled.amount - parsed.amount) / parsed.amount < AMOUNT_TOLERANCE
            val senderMatches = scheduled.sourceName?.let { sourceName ->
                senderName.contains(sourceName, ignoreCase = true) ||
                    sourceName.contains(senderName, ignoreCase = true)
            } ?: false
            amountClose && senderMatches
        }?.localId
    }

    companion object {
        private const val MATCH_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
        private const val AMOUNT_TOLERANCE = 0.01
    }
}
