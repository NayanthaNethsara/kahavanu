package com.kahavanu.data.notifications

import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.notifications.local.NotificationDao
import com.kahavanu.data.notifications.local.NotificationEntity
import com.kahavanu.domain.model.AppNotification
import com.kahavanu.domain.model.NotificationType
import com.kahavanu.domain.repository.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNotificationsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val notificationDao: NotificationDao,
) : NotificationsRepository {

    override fun observeNotifications(): Flow<List<AppNotification>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return notificationDao.observe(uid).map { list -> list.map { it.toDomain() } }
    }

    override fun observeUnreadCount(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return notificationDao.observeUnreadCount(uid)
    }

    override suspend fun record(type: NotificationType, title: String, message: String) {
        val uid = auth.currentUser?.uid ?: return
        notificationDao.insert(
            NotificationEntity(
                userId = uid,
                type = type.name,
                title = title,
                message = message,
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun markAllRead() {
        val uid = auth.currentUser?.uid ?: return
        notificationDao.markAllRead(uid)
    }

    override suspend fun clearAll() {
        val uid = auth.currentUser?.uid ?: return
        notificationDao.deleteAll(uid)
    }
}

private fun NotificationEntity.toDomain(): AppNotification = AppNotification(
    id = localId,
    type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.GENERAL),
    title = title,
    message = message,
    createdAtEpochMillis = createdAtEpochMillis,
    isRead = isRead,
)
