package com.kahavanu.data.income

import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType

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

fun IncomeSourceEntity.toDomain(): IncomeSource {
    val types = typesCsv
        .split(',')
        .mapNotNull { IncomeSourceType.fromId(it.trim()) }
        .toSet()
    return IncomeSource(
        id = localId,
        name = name,
        types = types,
    )
}
