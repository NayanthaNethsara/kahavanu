package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_income")
data class ScheduledIncomeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val frequency: String? = null,
    val scheduledDateEpochMillis: Long,
    val lastGeneratedEpochMillis: Long? = null,
    val sourceId: Long? = null,
    val sourceName: String? = null,
    val isInvoiceSent: Boolean = false,
    val contactName: String? = null,
    val contactNumber: String? = null,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)
