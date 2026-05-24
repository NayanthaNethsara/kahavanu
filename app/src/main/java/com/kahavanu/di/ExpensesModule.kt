package com.kahavanu.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.expenses.DefaultExpensesRepository
import com.kahavanu.data.expenses.DefaultSubscriptionsRepository
import com.kahavanu.data.expenses.local.ExpenseLogDao
import com.kahavanu.data.expenses.local.SubscriptionDao
import com.kahavanu.data.expenses.sync.ExpensesSyncManager
import com.kahavanu.data.expenses.sync.ExpensesSyncScheduler
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SubscriptionsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExpensesModule {

    @Binds
    @Singleton
    abstract fun bindExpensesRepository(
        defaultExpensesRepository: DefaultExpensesRepository,
    ): ExpensesRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionsRepository(
        defaultSubscriptionsRepository: DefaultSubscriptionsRepository,
    ): SubscriptionsRepository

    companion object {
        @Provides
        @Singleton
        fun provideExpensesSyncManager(
            @ApplicationContext context: Context,
            firestore: FirebaseFirestore,
            auth: FirebaseAuth,
            database: AppDatabase,
        ): ExpensesSyncManager = ExpensesSyncManager(context, firestore, auth, database)

        @Provides
        @Singleton
        fun provideExpensesSyncScheduler(
            @ApplicationContext context: Context,
        ): ExpensesSyncScheduler = ExpensesSyncScheduler(context)

        @Provides
        fun provideExpenseLogDao(
            database: AppDatabase,
        ): ExpenseLogDao = database.expenseLogDao()

        @Provides
        fun provideSubscriptionDao(
            database: AppDatabase,
        ): SubscriptionDao = database.subscriptionDao()
    }
}
