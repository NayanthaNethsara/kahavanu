package com.kahavanu.domain.repository

import com.kahavanu.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionsRepository {
    fun observeSubscriptions(): Flow<List<Subscription>>
    suspend fun upsertSubscription(subscription: Subscription): Result<Unit>
    suspend fun deleteSubscription(id: String): Result<Unit>
    suspend fun processSubscriptions(): Result<Unit>
}
