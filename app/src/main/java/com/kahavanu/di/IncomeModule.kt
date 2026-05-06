package com.kahavanu.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.DefaultIncomeRepository
import com.kahavanu.domain.repository.IncomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    }
}
