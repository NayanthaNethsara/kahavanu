package com.kahavanu.di

import android.content.Context
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.sieve.DefaultSmsScanRepository
import com.kahavanu.data.sieve.DefaultSmsSuggestionRepository
import com.kahavanu.data.sieve.local.SmsSuggestionDao
import com.kahavanu.data.sieve.sms.SmsScanScheduler
import com.kahavanu.data.sieve.sms.SmsReader
import com.kahavanu.domain.repository.SmsScanRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsSuggestionModule {

    @Binds
    @Singleton
    abstract fun bindSmsSuggestionRepository(
        impl: DefaultSmsSuggestionRepository,
    ): SmsSuggestionRepository

    @Binds
    @Singleton
    abstract fun bindSmsScanRepository(
        impl: DefaultSmsScanRepository,
    ): SmsScanRepository

    companion object {

        @Provides
        fun provideSmsSuggestionDao(database: AppDatabase): SmsSuggestionDao =
            database.smsSuggestionDao()

        @Provides
        @Singleton
        fun provideSmsReader(@ApplicationContext context: Context): SmsReader =
            SmsReader(context.contentResolver)

        @Provides
        @Singleton
        fun provideSmsScanScheduler(@ApplicationContext context: Context): SmsScanScheduler =
            SmsScanScheduler(context)
    }
}
