package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val userId: String,
    val primaryCurrency: String,
    val secondaryCurrency: String,
    val updatedAtEpochMillis: Long,
)
