package com.kahavanu.data.expenses

import com.kahavanu.data.expenses.local.ExpenseLogEntity
import com.kahavanu.domain.model.ExpenseLogEntry
import java.util.UUID

fun ExpenseLogEntity.toDomain(): ExpenseLogEntry = ExpenseLogEntry(
    title = title,
    amount = amount,
    currency = currency,
    spentAtEpochMillis = spentAtEpochMillis,
    merchant = merchant,
    category = category,
    notes = notes,
    paymentMethod = paymentMethod,
)

fun ExpenseLogEntry.toEntity(
    userId: String,
    createdAtEpochMillis: Long,
): ExpenseLogEntity = ExpenseLogEntity(
    userId = userId,
    title = title,
    amount = amount,
    currency = currency,
    spentAtEpochMillis = spentAtEpochMillis,
    createdAtEpochMillis = createdAtEpochMillis,
    merchant = merchant,
    category = category,
    notes = notes,
    paymentMethod = paymentMethod,
    clientId = generateClientId(),
)

fun ExpenseLogEntity.logKey(): String = buildExpenseLogKey(
    title = title,
    amount = amount,
    currency = currency,
    spentAtEpochMillis = spentAtEpochMillis,
    merchant = merchant,
    category = category,
    notes = notes,
    paymentMethod = paymentMethod,
)

fun buildExpenseLogKey(
    title: String,
    amount: Double,
    currency: String,
    spentAtEpochMillis: Long,
    merchant: String?,
    category: String,
    notes: String?,
    paymentMethod: String?,
): String {
    val normalizedTitle = title.trim().lowercase()
    val normalizedCurrency = currency.trim().lowercase()
    val normalizedMerchant = merchant?.trim()?.lowercase().orEmpty()
    val normalizedCategory = category.trim().lowercase()
    val normalizedNotes = notes?.trim()?.lowercase().orEmpty()
    val normalizedPaymentMethod = paymentMethod?.trim()?.lowercase().orEmpty()
    return listOf(
        normalizedTitle,
        amount.toString(),
        normalizedCurrency,
        spentAtEpochMillis.toString(),
        normalizedMerchant,
        normalizedCategory,
        normalizedNotes,
        normalizedPaymentMethod,
    ).joinToString("|")
}

fun generateClientId(): String = UUID.randomUUID().toString()
