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

fun Subscription.toEntity(
    userId: String,
    defaultClientId: String? = null,
    localId: Long = 0L,
    lastGeneratedEpochMillis: Long? = null,
    occurrenceCount: Int = 0
): SubscriptionEntity {
    return SubscriptionEntity(
        localId = localId,
        userId = userId,
        title = name,
        amount = cost,
        currency = currency,
        frequency = frequency,
        scheduledDateEpochMillis = scheduledDateEpochMillis,
        isPaused = isPaused,
        category = category,
        clientId = defaultClientId ?: id.ifBlank { java.util.UUID.randomUUID().toString() },
        lastGeneratedEpochMillis = lastGeneratedEpochMillis,
        occurrenceCount = occurrenceCount,
    )
}
