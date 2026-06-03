package com.kahavanu.data.notifications.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAtEpochMillis DESC LIMIT 100")
    fun observe(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun observeUnreadCount(userId: String): Flow<Int>

    @Insert
    suspend fun insert(entity: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId AND isRead = 0")
    suspend fun markAllRead(userId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
