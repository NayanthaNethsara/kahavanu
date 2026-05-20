package com.kahavanu.data.sieve.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.model.SuggestionStatus

@Entity(
    tableName = "sms_suggestions",
    indices = [
        Index("userId"),
        Index("smsBodyHash", unique = true),
    ],
)
data class SmsSuggestionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val userId: String,
    val smsSenderName: String,
    val smsBodyHash: String,
    val smsReceivedAtEpochMillis: Long,
    val kind: String,
    val amount: Double,
    val currency: String,
    val title: String,
    val merchant: String?,
    val txnAtEpochMillis: Long,
    val matchedScheduledIncomeId: Long?,
    val status: String,
    val confidence: Float,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
) {
    fun toDomain() = SmsSuggestion(
        localId = localId,
        userId = userId,
        smsSenderName = smsSenderName,
        smsBodyHash = smsBodyHash,
        smsReceivedAtEpochMillis = smsReceivedAtEpochMillis,
        kind = SuggestionKind.valueOf(kind),
        amount = amount,
        currency = currency,
        title = title,
        merchant = merchant,
        txnAtEpochMillis = txnAtEpochMillis,
        matchedScheduledIncomeId = matchedScheduledIncomeId,
        status = SuggestionStatus.valueOf(status),
        confidence = confidence,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}

fun SmsSuggestion.toEntity() = SmsSuggestionEntity(
    localId = localId,
    userId = userId,
    smsSenderName = smsSenderName,
    smsBodyHash = smsBodyHash,
    smsReceivedAtEpochMillis = smsReceivedAtEpochMillis,
    kind = kind.name,
    amount = amount,
    currency = currency,
    title = title,
    merchant = merchant,
    txnAtEpochMillis = txnAtEpochMillis,
    matchedScheduledIncomeId = matchedScheduledIncomeId,
    status = status.name,
    confidence = confidence,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)
