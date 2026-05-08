package com.kahavanu.domain.repository

import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun observeIncomeLogs(): Flow<List<IncomeLogEntry>>
    suspend fun logIncome(entry: IncomeLogEntry): Result<IncomeLogResult>

    fun observeIncomeSources(): Flow<List<IncomeSource>>
    suspend fun ensureDefaultSources()
    suspend fun upsertIncomeSource(source: IncomeSource): Result<Unit>
    suspend fun deleteIncomeSource(sourceId: Long): Result<Unit>
}
