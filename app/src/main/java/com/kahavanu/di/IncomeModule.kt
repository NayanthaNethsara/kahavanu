package com.kahavanu.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.DefaultIncomeRepository
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.data.local.AppDatabaseMigrations
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.sync.IncomeSyncManager
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
        fun provideAppDatabase(
            @ApplicationContext context: Context,
        ): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        )
            .addMigrations(
                AppDatabaseMigrations.MIGRATION_1_2,
                AppDatabaseMigrations.MIGRATION_2_3,
                AppDatabaseMigrations.MIGRATION_3_4,
                AppDatabaseMigrations.MIGRATION_4_5,
                AppDatabaseMigrations.MIGRATION_5_6,
                AppDatabaseMigrations.MIGRATION_6_7,
                AppDatabaseMigrations.MIGRATION_7_8,
                AppDatabaseMigrations.MIGRATION_9_10,
                AppDatabaseMigrations.MIGRATION_10_11,
                AppDatabaseMigrations.MIGRATION_11_12,
                AppDatabaseMigrations.MIGRATION_12_13,
                AppDatabaseMigrations.MIGRATION_13_14,
                AppDatabaseMigrations.MIGRATION_14_15,
                AppDatabaseMigrations.MIGRATION_15_16,
                AppDatabaseMigrations.MIGRATION_16_17,
                AppDatabaseMigrations.MIGRATION_17_18
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

        @Provides
        fun provideIncomeLogDao(
            database: AppDatabase,
        ): IncomeLogDao = database.incomeLogDao()

        @Provides
        fun provideIncomeSourceDao(
            database: AppDatabase,
        ): IncomeSourceDao = database.incomeSourceDao()

        @Provides
        fun provideScheduledIncomeDao(
            database: AppDatabase,
        ): com.kahavanu.data.income.local.ScheduledIncomeDao = database.scheduledIncomeDao()

        @Provides
        @Singleton
        fun provideIncomeSyncManager(
            @ApplicationContext context: Context,
            firestore: FirebaseFirestore,
            auth: FirebaseAuth,
            database: AppDatabase,
        ): IncomeSyncManager = IncomeSyncManager(context, firestore, auth, database)

        @Provides
        @Singleton
        fun provideIncomeSyncScheduler(
            @ApplicationContext context: Context,
        ): IncomeSyncScheduler = IncomeSyncScheduler(context)
    }
}
