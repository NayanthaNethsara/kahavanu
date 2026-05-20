package com.kahavanu.di

import com.kahavanu.sieve.engine.RuleBasedSmsClassifier
import com.kahavanu.sieve.engine.SmsClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsEngineModule {

    @Binds
    @Singleton
    abstract fun bindSmsClassifier(impl: RuleBasedSmsClassifier): SmsClassifier
}
