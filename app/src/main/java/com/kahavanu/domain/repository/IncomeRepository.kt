package com.kahavanu.domain.repository

import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.ui.income.CurrencyOption
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun observeIncomeLogs(): Flow<List<IncomeLogEntry>>
    suspend fun logIncome(entry: IncomeLogEntry): Result<IncomeLogResult>

    fun observeIncomeSources(): Flow<List<IncomeSource>>
    suspend fun ensureDefaultSources()
    suspend fun upsertIncomeSource(source: IncomeSource): Result<Unit>
    suspend fun deleteIncomeSource(sourceId: Long): Result<Unit>

    fun observeCurrencySettings(): Flow<Pair<CurrencyOption, CurrencyOption>>
    suspend fun updateCurrencySettings(primary: CurrencyOption, secondary: CurrencyOption): Result<Unit>

    // Scheduled & Recurrent Income
    fun observeScheduledIncomes(): Flow<List<ScheduledIncome>>
    suspend fun upsertScheduledIncome(scheduled: ScheduledIncome): Result<Unit>
    suspend fun deleteScheduledIncome(id: Long): Result<Unit>
    suspend fun markScheduledAsReceived(id: Long): Result<Unit>
    suspend fun processScheduledIncomes(): Result<Unit>
}
