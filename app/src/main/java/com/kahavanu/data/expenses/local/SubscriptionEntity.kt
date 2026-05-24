package com.kahavanu.data.expenses.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val frequency: String, // "monthly" or "yearly"
    val scheduledDateEpochMillis: Long,
    val lastGeneratedEpochMillis: Long? = null,
    val occurrenceCount: Int = 0,
    val category: String,
    val isPaused: Boolean = false,
    val clientId: String,
    override val remoteId: String? = null,
    override val isSynced: Boolean = false,
    override val isDeleted: Boolean = false,
    override val updatedAtEpochMillis: Long = System.currentTimeMillis(),
) : SyncEntity {
    override fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "amount" to amount,
        "currency" to currency,
        "frequency" to frequency,
        "scheduledDate" to scheduledDateEpochMillis,
        "lastGenerated" to lastGeneratedEpochMillis,
        "occurrenceCount" to occurrenceCount,
        "category" to category,
        "isPaused" to isPaused,
        "clientId" to clientId,
        "userId" to userId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
