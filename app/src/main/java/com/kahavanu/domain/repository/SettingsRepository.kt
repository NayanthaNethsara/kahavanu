package com.kahavanu.domain.repository

import com.kahavanu.domain.model.CurrencyOption
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeCurrencySettings(): Flow<Pair<CurrencyOption, CurrencyOption>>
    suspend fun updateCurrencySettings(primary: CurrencyOption, secondary: CurrencyOption): Result<Unit>
    suspend fun getLastSmsScanEpochMillis(): Long
    suspend fun updateLastSmsScanEpochMillis(epochMillis: Long)
    fun observeAutoMatchDeposits(): Flow<Boolean>
    suspend fun updateAutoMatchDeposits(enabled: Boolean): Result<Unit>
    fun observePushAlerts(): Flow<Boolean>
    suspend fun updatePushAlerts(enabled: Boolean): Result<Unit>
    fun observeMonthlyBudget(): Flow<Double>
    suspend fun updateMonthlyBudget(amount: Double): Result<Unit>
}
