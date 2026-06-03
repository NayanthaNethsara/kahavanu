package com.kahavanu.data.notifications.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"])],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean = false,
)
