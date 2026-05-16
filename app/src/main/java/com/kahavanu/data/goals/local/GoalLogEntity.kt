package com.kahavanu.data.goals.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(
    tableName = "goal_logs",
    indices = [
        Index("userId"),
        Index("clientId", unique = true),
    ],
)
data class GoalLogEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    val userId: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String,
    val category: String,
    val targetDateEpochMillis: Long?,
    val isCompleted: Boolean,
    val createdAtEpochMillis: Long,
    val clientId: String,
    override val remoteId: String? = null,
    override val isSynced: Boolean = false,
    override val isDeleted: Boolean = false,
    override val updatedAtEpochMillis: Long = System.currentTimeMillis(),
) : SyncEntity {
    override fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "targetAmount" to targetAmount,
        "currentAmount" to currentAmount,
        "currency" to currency,
        "category" to category,
        "targetDate" to targetDateEpochMillis,
        "isCompleted" to isCompleted,
        "createdAt" to createdAtEpochMillis,
        "clientId" to clientId,
        "userId" to userId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
