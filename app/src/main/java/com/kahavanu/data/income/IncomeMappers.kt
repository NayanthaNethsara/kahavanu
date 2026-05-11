package com.kahavanu.data.income

import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType

import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.domain.model.ScheduledIncome
import java.util.UUID

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
    clientId = generateClientId(),
)

fun IncomeLogEntity.logKey(): String = buildIncomeLogKey(
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

fun buildIncomeLogKey(
    title: String,
    amount: Double,
    currency: String,
    receivedAtEpochMillis: Long,
    sourceId: Long?,
    sourceName: String?,
    sourceType: String?,
    isInvoiceSent: Boolean,
    frequency: String?,
    contactName: String?,
    contactNumber: String?,
): String {
    val normalizedTitle = title.trim().lowercase()
    val normalizedCurrency = currency.trim().lowercase()
    val normalizedSourceName = sourceName?.trim()?.lowercase().orEmpty()
    val normalizedSourceType = sourceType?.trim()?.lowercase().orEmpty()
    val normalizedFrequency = frequency?.trim()?.lowercase().orEmpty()
    val normalizedContactName = contactName?.trim()?.lowercase().orEmpty()
    val normalizedContactNumber = contactNumber?.trim().orEmpty()
    return listOf(
        normalizedTitle,
        amount.toString(),
        normalizedCurrency,
        receivedAtEpochMillis.toString(),
        sourceId?.toString().orEmpty(),
        normalizedSourceName,
        normalizedSourceType,
        isInvoiceSent.toString(),
        normalizedFrequency,
        normalizedContactName,
        normalizedContactNumber,
    ).joinToString("|")
}

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
    occurrenceCount = occurrenceCount,
    sourceId = sourceId,
    sourceName = sourceName,
    isInvoiceSent = isInvoiceSent,
    contactName = contactName,
    contactNumber = contactNumber,
)

fun ScheduledIncome.toEntity(
    userId: String,
    clientId: String = generateClientId(),
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
    occurrenceCount = occurrenceCount,
    sourceId = sourceId,
    sourceName = sourceName,
    isInvoiceSent = isInvoiceSent,
    contactName = contactName,
    contactNumber = contactNumber,
    clientId = clientId,
)

fun generateClientId(): String = UUID.randomUUID().toString()

fun normalizeTypesCsv(typesCsv: String): String = normalizeTypesCsv(
    typesCsv.split(',')
)

fun normalizeTypesCsv(types: Collection<String>): String {
    return types
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .sorted()
        .joinToString(",")
}
