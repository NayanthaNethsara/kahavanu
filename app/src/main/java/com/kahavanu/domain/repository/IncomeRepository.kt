package com.kahavanu.domain.repository

import com.kahavanu.domain.model.IncomeLogEntry

interface IncomeRepository {
    suspend fun logIncome(entry: IncomeLogEntry): Result<Unit>
}
