package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income_logs",
    indices = [Index("userId")]
)
data class IncomeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val note: String?,
    val receivedAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
)
