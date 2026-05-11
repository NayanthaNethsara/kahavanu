package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kahavanu.data.sync.SyncEntity

@Entity(
    tableName = "income_sources",
    indices = [Index("userId")],
)
data class IncomeSourceEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    val userId: String,
    val name: String,
    val typesCsv: String,
    val createdAtEpochMillis: Long,
    override val updatedAtEpochMillis: Long,
    override val remoteId: String? = null,
    override val isSynced: Boolean = false,
    override val isDeleted: Boolean = false,
) : SyncEntity {
    override fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "types" to typesCsv.split(',').filter { it.isNotBlank() },
        "createdAt" to createdAtEpochMillis,
        "updatedAt" to updatedAtEpochMillis,
        "userId" to userId,
    )
}
