package com.kahavanu.domain.repository

import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun observeIncomeLogs(): Flow<List<IncomeLogEntry>>
    suspend fun logIncome(entry: IncomeLogEntry): Result<IncomeLogResult>
}
