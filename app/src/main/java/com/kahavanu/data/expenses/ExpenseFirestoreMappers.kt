package com.kahavanu.data.expenses

import com.google.firebase.firestore.DocumentSnapshot
import com.kahavanu.data.expenses.local.ExpenseLogEntity

fun DocumentSnapshot.toExpenseLogEntity(
    uid: String,
    remoteId: String,
    localId: Long,
): ExpenseLogEntity {
    val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    val updatedAt = getLong("updatedAt") ?: createdAt
    val clientId = getString("clientId") ?: remoteId
    val title = getString("title")
        ?: getString("description")
        ?: getString("name")
        ?: ""
    val amount = getDouble("amount")
        ?: getLong("amount")?.toDouble()
        ?: (get("amount") as? Number)?.toDouble()
        ?: 0.0
    val currency = getString("currency") ?: ""
    val spentAt = getLong("spentAt")
        ?: getLong("spentAtEpochMillis")
        ?: getLong("receivedAt")
        ?: System.currentTimeMillis()

    return ExpenseLogEntity(
        localId = localId,
        userId = uid,
        title = title,
        amount = amount,
        currency = currency,
        spentAtEpochMillis = spentAt,
        createdAtEpochMillis = createdAt,
        merchant = getString("merchant"),
        category = getString("category") ?: "",
        notes = getString("notes"),
        paymentMethod = getString("paymentMethod"),
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
        updatedAtEpochMillis = updatedAt,
    )
}
