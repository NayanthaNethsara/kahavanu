package com.kahavanu.data.goals

import com.google.firebase.firestore.DocumentSnapshot
import com.kahavanu.data.goals.local.GoalLogEntity

fun DocumentSnapshot.toGoalLogEntity(
    uid: String,
    remoteId: String,
    localId: Long = 0L,
): GoalLogEntity {
    val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    val updatedAt = getLong("updatedAt") ?: createdAt
    val clientId = getString("clientId") ?: remoteId
    return GoalLogEntity(
        localId = localId,
        userId = uid,
        title = getString("title") ?: "",
        targetAmount = getDouble("targetAmount") ?: 0.0,
        currentAmount = getDouble("currentAmount") ?: 0.0,
        currency = getString("currency") ?: "LKR",
        category = getString("category") ?: "OTHER",
        targetDateEpochMillis = getLong("targetDate"),
        isCompleted = getBoolean("isCompleted") ?: false,
        createdAtEpochMillis = createdAt,
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
        updatedAtEpochMillis = updatedAt,
    )
}
