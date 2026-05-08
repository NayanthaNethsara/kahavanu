package com.kahavanu.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.DefaultIncomeRepository
import com.kahavanu.data.income.local.IncomeDatabase
import com.kahavanu.data.income.local.IncomeDatabaseMigrations
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.sync.IncomeSyncScheduler
import com.kahavanu.domain.repository.IncomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IncomeModule {

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        defaultIncomeRepository: DefaultIncomeRepository,
    ): IncomeRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

        @Provides
        @Singleton
        fun provideIncomeDatabase(
            @ApplicationContext context: Context,
        ): IncomeDatabase = Room.databaseBuilder(
            context,
            IncomeDatabase::class.java,
            IncomeDatabase.DB_NAME,
        )
            .addMigrations(IncomeDatabaseMigrations.MIGRATION_1_2)
            .build()

        @Provides
        fun provideIncomeLogDao(
            database: IncomeDatabase,
        ): IncomeLogDao = database.incomeLogDao()

        @Provides
        fun provideIncomeSourceDao(
            database: IncomeDatabase,
        ): IncomeSourceDao = database.incomeSourceDao()

        @Provides
        @Singleton
        fun provideIncomeSyncScheduler(
            @ApplicationContext context: Context,
        ): IncomeSyncScheduler = IncomeSyncScheduler(context)
    }
}
