package com.kahavanu.data.income

import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType

import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.domain.model.ScheduledIncome

fun IncomeLogEntity.toDomain(): IncomeLogEntry = IncomeLogEntry(
    title = title,
    amount = amount,
    currency = currency,
    receivedAtEpochMillis = receivedAtEpochMillis,
    sourceId = sourceId,
    sourceName = sourceName,
    sourceType = sourceType,
    isInvoiceSent = isInvoiceSent,
    frequency = frequency,
    contactName = contactName,
    contactNumber = contactNumber,
)

fun IncomeLogEntry.toEntity(
    userId: String,
    createdAtEpochMillis: Long,
): IncomeLogEntity = IncomeLogEntity(
    userId = userId,
    title = title,
    amount = amount,
    currency = currency,
    receivedAtEpochMillis = receivedAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    sourceId = sourceId,
    sourceName = sourceName,
    sourceType = sourceType,
    isInvoiceSent = isInvoiceSent,
    frequency = frequency,
    contactName = contactName,
    contactNumber = contactNumber,
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

fun ScheduledIncomeEntity.toDomain(): ScheduledIncome = ScheduledIncome(
    id = localId,
    title = title,
    amount = amount,
    currency = currency,
    type = IncomeSourceType.fromId(type) ?: IncomeSourceType.PENDING,
    frequency = frequency,
    scheduledDateEpochMillis = scheduledDateEpochMillis,
    lastGeneratedEpochMillis = lastGeneratedEpochMillis,
    sourceId = sourceId,
    sourceName = sourceName,
    isInvoiceSent = isInvoiceSent,
    contactName = contactName,
    contactNumber = contactNumber,
)

fun ScheduledIncome.toEntity(
    userId: String,
): ScheduledIncomeEntity = ScheduledIncomeEntity(
    localId = id,
    userId = userId,
    title = title,
    amount = amount,
    currency = currency,
    type = type.id,
    frequency = frequency,
    scheduledDateEpochMillis = scheduledDateEpochMillis,
    lastGeneratedEpochMillis = lastGeneratedEpochMillis,
    sourceId = sourceId,
    sourceName = sourceName,
    isInvoiceSent = isInvoiceSent,
    contactName = contactName,
    contactNumber = contactNumber,
)
