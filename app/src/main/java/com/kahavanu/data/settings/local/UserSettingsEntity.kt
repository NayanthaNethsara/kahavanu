package com.kahavanu.data.settings.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val userId: String,
    val primaryCurrency: String,
    val secondaryCurrency: String,
    val updatedAtEpochMillis: Long,
    val lastSmsScanEpochMillis: Long = 0L,
    val isAutoMatchDepositsEnabled: Boolean = true,
    val isPushAlertsEnabled: Boolean = true,
)