package com.kahavanu.di

import com.kahavanu.sieve.engine.SmsClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmsEngineModule {

    @Provides
    @Singleton
    fun provideSmsClassifier(): SmsClassifier {
        // RuleBasedSmsClassifier will replace this stub in Part 2
        return object : SmsClassifier {
            override fun classify(raw: com.kahavanu.sieve.engine.RawSms) = null
        }
    }
}
