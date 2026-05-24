package com.kahavanu.data.expenses

import com.kahavanu.data.expenses.local.SubscriptionEntity
import com.kahavanu.domain.model.Subscription
import com.kahavanu.ui.util.formatDate

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = clientId,
        name = title,
        cost = amount,
        currency = currency,
        frequency = frequency,
        nextBillingDate = formatDate(scheduledDateEpochMillis),
        isPaused = isPaused,
        category = category,
        scheduledDateEpochMillis = scheduledDateEpochMillis,
    )
}

fun Subscription.toEntity(userId: String, defaultClientId: String? = null): SubscriptionEntity {
    return SubscriptionEntity(
        userId = userId,
        title = name,
        amount = cost,
        currency = currency,
        frequency = frequency,
        scheduledDateEpochMillis = scheduledDateEpochMillis,
        isPaused = isPaused,
        category = category,
        clientId = defaultClientId ?: id.ifBlank { java.util.UUID.randomUUID().toString() },
    )
}
