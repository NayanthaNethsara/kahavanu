package com.kahavanu.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.goals.DefaultGoalsRepository
import com.kahavanu.data.goals.local.GoalLogDao
import com.kahavanu.data.goals.sync.GoalsSyncManager
import com.kahavanu.data.goals.sync.GoalsSyncScheduler
import com.kahavanu.data.local.AppDatabase
import com.kahavanu.domain.repository.GoalsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GoalsModule {

    @Binds
    @Singleton
    abstract fun bindGoalsRepository(
        impl: DefaultGoalsRepository,
    ): GoalsRepository

    companion object {
        @Provides
        fun provideGoalLogDao(database: AppDatabase): GoalLogDao = database.goalLogDao()

        @Provides
        @Singleton
        fun provideGoalsSyncManager(
            @ApplicationContext context: Context,
            firestore: FirebaseFirestore,
            auth: FirebaseAuth,
            database: AppDatabase,
        ): GoalsSyncManager = GoalsSyncManager(context, firestore, auth, database)

        @Provides
        @Singleton
        fun provideGoalsSyncScheduler(
            @ApplicationContext context: Context,
        ): GoalsSyncScheduler = GoalsSyncScheduler(context)
    }
}
