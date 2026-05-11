package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(
    tableName = "income_logs",
    indices = [Index("userId")]
)
data class IncomeLogEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val receivedAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val sourceId: Long? = null,
    val sourceName: String? = null,
    val sourceType: String? = null,
    val isInvoiceSent: Boolean = false,
    val frequency: String? = null,
    val contactName: String? = null,
    val contactNumber: String? = null,
    override val remoteId: String? = null,
    override val isSynced: Boolean = false,
    override val isDeleted: Boolean = false,
    override val updatedAtEpochMillis: Long = System.currentTimeMillis(),
) : SyncEntity {
    override fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "amount" to amount,
        "currency" to currency,
        "receivedAt" to receivedAtEpochMillis,
        "createdAt" to createdAtEpochMillis,
        "sourceId" to sourceId,
        "sourceName" to sourceName,
        "sourceType" to sourceType,
        "isInvoiceSent" to isInvoiceSent,
        "frequency" to frequency,
        "contactName" to contactName,
        "contactNumber" to contactNumber,
        "userId" to userId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
