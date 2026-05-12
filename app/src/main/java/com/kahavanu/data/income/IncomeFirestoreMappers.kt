package com.kahavanu.data.income

import com.google.firebase.firestore.DocumentSnapshot
import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.ScheduledIncomeEntity

fun DocumentSnapshot.toIncomeLogEntity(
    uid: String,
    remoteId: String,
    localId: Long = 0L,
): IncomeLogEntity {
    val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    val updatedAt = getLong("updatedAt") ?: createdAt
    val clientId = getString("clientId") ?: remoteId
    return IncomeLogEntity(
        localId = localId,
        userId = uid,
        title = getString("title") ?: "",
        amount = getDouble("amount") ?: 0.0,
        currency = getString("currency") ?: "",
        receivedAtEpochMillis = getLong("receivedAt") ?: System.currentTimeMillis(),
        createdAtEpochMillis = createdAt,
        sourceId = getLong("sourceId"),
        sourceName = getString("sourceName"),
        sourceType = getString("sourceType"),
        isInvoiceSent = getBoolean("isInvoiceSent") ?: false,
        frequency = getString("frequency"),
        contactName = getString("contactName"),
        contactNumber = getString("contactNumber"),
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
        updatedAtEpochMillis = updatedAt,
    )
}

fun DocumentSnapshot.toIncomeSourceEntity(
    uid: String,
    remoteId: String,
    localId: Long = 0L,
): IncomeSourceEntity {
    val typesList = (get("types") as? List<*>)
        ?.mapNotNull { it as? String }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
    val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    val updatedAt = getLong("updatedAt") ?: createdAt
    val clientId = getString("clientId") ?: remoteId
    return IncomeSourceEntity(
        localId = localId,
        userId = uid,
        name = getString("name") ?: "",
        typesCsv = normalizeTypesCsv(typesList),
        createdAtEpochMillis = createdAt,
        updatedAtEpochMillis = updatedAt,
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
    )
}

fun DocumentSnapshot.toScheduledIncomeEntity(
    uid: String,
    remoteId: String,
    localId: Long = 0L,
): ScheduledIncomeEntity {
    val updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
    val clientId = getString("clientId") ?: remoteId
    val title = getString("title")
        ?: getString("clientDescription")
        ?: getString("description")
        ?: getString("name")
        ?: ""
    val amount = getDouble("amount")
        ?: getLong("amount")?.toDouble()
        ?: (get("amount") as? Number)?.toDouble()
        ?: 0.0
    val currency = getString("currency")
        ?: getString("currencyCode")
        ?: ""
    val type = getString("type")
        ?: getString("incomeType")
        ?: "pending"
    val scheduledDate = getLong("scheduledDate")
        ?: getLong("scheduledDateEpochMillis")
        ?: getLong("scheduledAt")
        ?: System.currentTimeMillis()
    val lastGenerated = getLong("lastGenerated")
        ?: getLong("lastGeneratedEpochMillis")
        ?: getLong("receivedAt")
    return ScheduledIncomeEntity(
        localId = localId,
        userId = uid,
        title = title,
        amount = amount,
        currency = currency,
        type = type,
        frequency = getString("frequency"),
        scheduledDateEpochMillis = scheduledDate,
        lastGeneratedEpochMillis = lastGenerated,
        occurrenceCount = (getLong("occurrenceCount") ?: 0L).toInt(),
        sourceId = getLong("sourceId"),
        sourceName = getString("sourceName"),
        isInvoiceSent = getBoolean("isInvoiceSent") ?: false,
        contactName = getString("contactName"),
        contactNumber = getString("contactNumber"),
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
        updatedAtEpochMillis = updatedAt,
    )
}
