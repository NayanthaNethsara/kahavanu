package com.kahavanu.domain.repository

import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.ExpenseLogResult
import com.kahavanu.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface ExpensesRepository {
    fun observeExpenseLogs(): Flow<List<ExpenseLogEntry>>
    suspend fun logExpense(entry: ExpenseLogEntry): Result<ExpenseLogResult>

    // Subscriptions
    fun observeSubscriptions(): Flow<List<Subscription>>
    suspend fun upsertSubscription(subscription: Subscription): Result<Unit>
    suspend fun deleteSubscription(id: String): Result<Unit>
    suspend fun processSubscriptions(): Result<Unit>
}
