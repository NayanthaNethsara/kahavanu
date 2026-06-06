package com.kahavanu.di

import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.notifications.DefaultNotificationsRepository
import com.kahavanu.data.notifications.local.NotificationDao
import com.kahavanu.domain.repository.NotificationsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        defaultNotificationsRepository: DefaultNotificationsRepository,
    ): NotificationsRepository

    companion object {
        @Provides
        fun provideNotificationDao(
            database: AppDatabase,
        ): NotificationDao = database.notificationDao()
    }
}
