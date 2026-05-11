package com.kahavanu.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.DefaultIncomeRepository
import com.kahavanu.data.income.local.IncomeDatabase
import com.kahavanu.data.income.local.IncomeDatabaseMigrations
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
        fun provideIncomeDatabase(
            @ApplicationContext context: Context,
        ): IncomeDatabase = Room.databaseBuilder(
            context,
            IncomeDatabase::class.java,
            IncomeDatabase.DB_NAME,
        )
            .addMigrations(
                IncomeDatabaseMigrations.MIGRATION_1_2,
                IncomeDatabaseMigrations.MIGRATION_2_3,
                IncomeDatabaseMigrations.MIGRATION_3_4,
                IncomeDatabaseMigrations.MIGRATION_4_5,
                IncomeDatabaseMigrations.MIGRATION_5_6,
                IncomeDatabaseMigrations.MIGRATION_6_7,
                IncomeDatabaseMigrations.MIGRATION_7_8,
                IncomeDatabaseMigrations.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
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
        fun provideScheduledIncomeDao(
            database: IncomeDatabase,
        ): com.kahavanu.data.income.local.ScheduledIncomeDao = database.scheduledIncomeDao()

        @Provides
        @Singleton
        fun provideIncomeSyncManager(
            @ApplicationContext context: Context,
            firestore: FirebaseFirestore,
            auth: FirebaseAuth,
            database: IncomeDatabase,
        ): IncomeSyncManager = IncomeSyncManager(context, firestore, auth, database)

        @Provides
        @Singleton
        fun provideIncomeSyncScheduler(
            @ApplicationContext context: Context,
        ): IncomeSyncScheduler = IncomeSyncScheduler(context)
    }
}
