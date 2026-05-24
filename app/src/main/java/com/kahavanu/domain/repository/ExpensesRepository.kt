package com.kahavanu.domain.repository

import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.ExpenseLogResult
import kotlinx.coroutines.flow.Flow

interface ExpensesRepository {
    fun observeExpenseLogs(): Flow<List<ExpenseLogEntry>>
    suspend fun logExpense(entry: ExpenseLogEntry): Result<ExpenseLogResult>
}
