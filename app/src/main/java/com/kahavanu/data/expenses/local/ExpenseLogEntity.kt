package com.kahavanu.data.expenses.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(
    tableName = "expense_logs",
    indices = [Index("userId")],
)
data class ExpenseLogEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val spentAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val merchant: String? = null,
    val category: String,
    val notes: String? = null,
    val paymentMethod: String? = null,
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
        "spentAt" to spentAtEpochMillis,
        "createdAt" to createdAtEpochMillis,
        "merchant" to merchant,
        "category" to category,
        "notes" to notes,
        "paymentMethod" to paymentMethod,
        "clientId" to clientId,
        "userId" to userId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
