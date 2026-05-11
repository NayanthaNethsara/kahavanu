package com.kahavanu.di

import android.content.Context
import com.kahavanu.data.income.sync.IncomeSyncManager
import com.kahavanu.data.sync.SyncInitializer
import com.kahavanu.data.sync.SyncManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncManager(
        incomeSyncManager: IncomeSyncManager,
    ): SyncManager

    companion object {
        @Provides
        @Singleton
        fun provideSyncInitializer(
            @ApplicationContext context: Context,
            syncManager: SyncManager,
        ): SyncInitializer {
            val initializer = SyncInitializer(context, syncManager)
            initializer.init()
            return initializer
        }
    }
}
