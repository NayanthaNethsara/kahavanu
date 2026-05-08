package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income_sources",
    indices = [Index("userId")],
)
data class IncomeSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val userId: String,
    val name: String,
    val typesCsv: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)
