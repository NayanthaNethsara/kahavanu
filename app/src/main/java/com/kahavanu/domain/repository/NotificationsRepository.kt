package com.kahavanu.domain.repository

import com.kahavanu.domain.model.AppNotification
import com.kahavanu.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    fun observeNotifications(): Flow<List<AppNotification>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun record(type: NotificationType, title: String, message: String)
    suspend fun markAllRead()
    suspend fun clearAll()
}
