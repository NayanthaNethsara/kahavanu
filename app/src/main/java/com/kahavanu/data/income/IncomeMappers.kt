package com.kahavanu.data.income

import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.domain.model.IncomeLogEntry

fun IncomeLogEntity.toDomain(): IncomeLogEntry = IncomeLogEntry(
    title = title,
    amount = amount,
    currency = currency,
    note = note,
    receivedAtEpochMillis = receivedAtEpochMillis,
)

fun IncomeLogEntry.toEntity(
    userId: String,
    createdAtEpochMillis: Long,
): IncomeLogEntity = IncomeLogEntity(
    userId = userId,
    title = title,
    amount = amount,
    currency = currency,
    note = note,
    receivedAtEpochMillis = receivedAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
)
