package com.kahavanu.data.sieve.sms

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.sieve.local.SmsSuggestionEntity
import com.kahavanu.data.sieve.local.SmsSuggestionDao
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.model.SuggestionStatus
import com.kahavanu.domain.repository.SmsSenderRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.sieve.engine.PendingMatcher
import com.kahavanu.sieve.engine.RawSms
import com.kahavanu.sieve.engine.SmsClassifier
import com.kahavanu.sieve.engine.SmsHasher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SmsScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val smsReader: SmsReader,
    private val classifier: SmsClassifier,
    private val pendingMatcher: PendingMatcher,
    private val smsSuggestionDao: SmsSuggestionDao,
    private val smsSenderRepository: SmsSenderRepository,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = auth.currentUser?.uid ?: return Result.failure()

        val enabledSenders = smsSenderRepository.observeAuthorizedSenders()
            .first()
            .filter { it.isEnabled }
            .map { it.senderName }
            .toSet()

        if (enabledSenders.isEmpty()) return Result.success()

        val sinceMillis = settingsRepository.getLastSmsScanEpochMillis()
        val now = System.currentTimeMillis()

        val messages = smsReader.readSince(enabledSenders, sinceMillis)

        for (raw in messages) {
            processSms(uid, raw)
        }

        settingsRepository.updateLastSmsScanEpochMillis(now)
        return Result.success()
    }

    private suspend fun processSms(userId: String, raw: RawSms) {
        val hash = SmsHasher.hash(raw.senderName, raw.body, raw.receivedAtEpochMillis)
        if (smsSuggestionDao.existsByHash(hash)) return

        val parsed = classifier.classify(raw) ?: return

        val matchedId = pendingMatcher.findMatch(parsed, raw.senderName)
        val effectiveKind = if (matchedId != null) SuggestionKind.SETTLE_PENDING else parsed.kind

        val now = System.currentTimeMillis()
        smsSuggestionDao.insertIfNew(
            SmsSuggestionEntity(
                userId = userId,
                smsSenderName = raw.senderName,
                smsBodyHash = hash,
                smsReceivedAtEpochMillis = raw.receivedAtEpochMillis,
                kind = effectiveKind.name,
                amount = parsed.amount,
                currency = parsed.currency,
                title = parsed.title,
                merchant = parsed.merchant,
                txnAtEpochMillis = parsed.txnAtEpochMillis,
                matchedScheduledIncomeId = matchedId,
                status = SuggestionStatus.PENDING.name,
                confidence = parsed.confidence,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        )
    }
}
