package com.kahavanu.data.goals

import com.kahavanu.data.goals.local.GoalLogEntity
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.domain.model.GoalEntry
import com.kahavanu.domain.model.CurrencyOption
import java.util.UUID

fun GoalLogEntity.toDomain(): GoalEntry = GoalEntry(
    id = remoteId ?: clientId,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = CurrencyOption.entries.firstOrNull { it.code == currency } ?: CurrencyOption.LKR,
    category = GoalCategory.entries.firstOrNull { it.name == category } ?: GoalCategory.OTHER,
    targetDateEpochMillis = targetDateEpochMillis,
    isCompleted = isCompleted,
    createdAtEpochMillis = createdAtEpochMillis,
    lastUpdatedEpochMillis = updatedAtEpochMillis,
)

fun GoalEntry.toEntity(userId: String): GoalLogEntity = GoalLogEntity(
    userId = userId,
    title = title,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = currency.code,
    category = category.name,
    targetDateEpochMillis = targetDateEpochMillis,
    isCompleted = isCompleted,
    createdAtEpochMillis = createdAtEpochMillis,
    clientId = id.ifBlank { UUID.randomUUID().toString() },
)

fun generateGoalClientId(): String = UUID.randomUUID().toString()
