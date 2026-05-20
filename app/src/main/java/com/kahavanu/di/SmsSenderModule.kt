package com.kahavanu.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.sieve.DefaultSmsSenderRepository
import com.kahavanu.data.sieve.local.SmsSenderDao
import com.kahavanu.data.sieve.sync.SmsSenderSyncManager
import com.kahavanu.data.sieve.sync.SmsSenderSyncScheduler
import com.kahavanu.domain.repository.SmsSenderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsSenderModule {

    @Binds
    @Singleton
    abstract fun bindSmsSenderRepository(
        impl: DefaultSmsSenderRepository,
    ): SmsSenderRepository

    companion object {
        @Provides
        @Singleton
        fun provideSmsSenderDao(
            database: AppDatabase,
        ): SmsSenderDao = database.smsSenderDao()

        @Provides
        @Singleton
        fun provideSmsSenderSyncManager(
            @ApplicationContext context: Context,
            firestore: FirebaseFirestore,
            auth: FirebaseAuth,
            database: AppDatabase,
        ): SmsSenderSyncManager = SmsSenderSyncManager(context, firestore, auth, database)

        @Provides
        @Singleton
        fun provideSmsSenderSyncScheduler(
            @ApplicationContext context: Context,
        ): SmsSenderSyncScheduler = SmsSenderSyncScheduler(context)
    }
}
