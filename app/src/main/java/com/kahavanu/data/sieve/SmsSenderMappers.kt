package com.kahavanu.data.sieve

import com.google.firebase.firestore.DocumentSnapshot
import com.kahavanu.data.sieve.local.SmsSenderEntity
import com.kahavanu.domain.model.SmsSender
import java.util.UUID

fun SmsSenderEntity.toDomain(): SmsSender = SmsSender(
    id = remoteId ?: clientId,
    senderName = senderName,
    isEnabled = isEnabled,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun SmsSender.toEntity(userId: String): SmsSenderEntity = SmsSenderEntity(
    userId = userId,
    senderName = senderName,
    isEnabled = isEnabled,
    createdAtEpochMillis = createdAtEpochMillis,
    clientId = id.ifBlank { UUID.randomUUID().toString() },
)

fun DocumentSnapshot.toSmsSenderEntity(
    uid: String,
    remoteId: String,
    localId: Long = 0L,
): SmsSenderEntity {
    val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
    val updatedAt = getLong("updatedAt") ?: createdAt
    val clientId = getString("clientId") ?: remoteId
    return SmsSenderEntity(
        localId = localId,
        userId = uid,
        senderName = getString("senderName") ?: "",
        isEnabled = getBoolean("isEnabled") ?: true,
        createdAtEpochMillis = createdAt,
        clientId = clientId,
        remoteId = remoteId,
        isSynced = true,
        isDeleted = false,
        updatedAtEpochMillis = updatedAt,
    )
}
