package com.kahavanu.data.sieve.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(
    tableName = "sms_senders",
    indices = [
        Index("userId"),
        Index("clientId", unique = true)
    ],
)
data class SmsSenderEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    val userId: String,
    val senderName: String,
    val isEnabled: Boolean,
    val createdAtEpochMillis: Long,
    val clientId: String,
    override val remoteId: String? = null,
    override val isSynced: Boolean = false,
    override val isDeleted: Boolean = false,
    override val updatedAtEpochMillis: Long = System.currentTimeMillis(),
) : SyncEntity {
    override fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "senderName" to senderName,
        "isEnabled" to isEnabled,
        "createdAt" to createdAtEpochMillis,
        "clientId" to clientId,
        "updatedAt" to updatedAtEpochMillis,
    )
}
