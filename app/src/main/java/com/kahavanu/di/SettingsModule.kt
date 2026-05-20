package com.kahavanu.di

import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.settings.local.UserSettingsDao
import com.kahavanu.data.settings.DefaultSettingsRepository
import com.kahavanu.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        defaultSettingsRepository: DefaultSettingsRepository,
    ): SettingsRepository

    companion object {
        @Provides
        fun provideUserSettingsDao(
            database: AppDatabase,
        ): UserSettingsDao = database.userSettingsDao()
    }
}
