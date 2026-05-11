package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(tableName = "scheduled_income")
data class ScheduledIncomeEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val frequency: String? = null,
    val scheduledDateEpochMillis: Long,
    val lastGeneratedEpochMillis: Long? = null,
    val occurrenceCount: Int = 0,
    val sourceId: Long? = null,
    val sourceName: String? = null,
    val isInvoiceSent: Boolean = false,
    val contactName: String? = null,
    val contactNumber: String? = null,
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
        "type" to type,
        "frequency" to frequency,
        "scheduledDate" to scheduledDateEpochMillis,
        "lastGenerated" to lastGeneratedEpochMillis,
        "occurrenceCount" to occurrenceCount,
        "sourceId" to sourceId,
        "sourceName" to sourceName,
        "isInvoiceSent" to isInvoiceSent,
        "contactName" to contactName,
        "contactNumber" to contactNumber,
        "clientId" to clientId,
        "userId" to userId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
