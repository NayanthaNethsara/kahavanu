package com.kahavanu.data.settings.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun observeSettings(userId: String): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    suspend fun getSettings(userId: String): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET lastSmsScanEpochMillis = :epochMillis WHERE userId = :userId")
    suspend fun updateLastSmsScan(userId: String, epochMillis: Long)

    @Query("SELECT lastSmsScanEpochMillis FROM user_settings WHERE userId = :userId")
    suspend fun getLastSmsScan(userId: String): Long?
}