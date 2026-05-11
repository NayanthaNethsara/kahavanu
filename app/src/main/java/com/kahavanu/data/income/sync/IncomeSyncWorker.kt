package com.kahavanu.data.income.sync

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result as WorkResult
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.income.local.IncomeDatabase
import com.kahavanu.data.income.local.IncomeDatabaseMigrations
import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.ScheduledIncomeEntity
import kotlinx.coroutines.suspendCancellableCoroutine

class IncomeSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): WorkResult {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return WorkResult.success()
        val database = Room.databaseBuilder(
            applicationContext,
            IncomeDatabase::class.java,
            IncomeDatabase.DB_NAME,
        )
            .addMigrations(
                IncomeDatabaseMigrations.MIGRATION_1_2,
                IncomeDatabaseMigrations.MIGRATION_2_3,
                IncomeDatabaseMigrations.MIGRATION_3_4,
                IncomeDatabaseMigrations.MIGRATION_4_5,
                IncomeDatabaseMigrations.MIGRATION_5_6,
                IncomeDatabaseMigrations.MIGRATION_6_7,
                IncomeDatabaseMigrations.MIGRATION_7_8
            )
            .build()

        return try {
            val firestore = FirebaseFirestore.getInstance()
            val logResult = syncIncomeLogs(uid, firestore, database)
            if (logResult is WorkResult.Retry) return WorkResult.retry()

            val sourceResult = syncIncomeSources(uid, firestore, database)
            if (sourceResult is WorkResult.Retry) return WorkResult.retry()

            val scheduledResult = syncScheduledIncomes(uid, firestore, database)
            if (scheduledResult is WorkResult.Retry) return WorkResult.retry()

            WorkResult.success()
        } catch (_: Exception) {
            WorkResult.retry()
        } finally {
            database.close()
        }
    }

    private suspend fun syncIncomeLogs(
        uid: String,
        firestore: FirebaseFirestore,
        database: IncomeDatabase,
    ): WorkResult {
        val dao = database.incomeLogDao()
        val unsynced = dao.getUnsynced(uid)
        if (unsynced.isEmpty()) {
            return pullRemoteIncomeLogs(uid, firestore, database)
        }

        for (entry in unsynced) {
            val data = mapOf(
                "title" to entry.title,
                "amount" to entry.amount,
                "currency" to entry.currency,
                "receivedAt" to entry.receivedAtEpochMillis,
                "createdAt" to entry.createdAtEpochMillis,
                "userId" to uid,
                "sourceId" to entry.sourceId,
                "sourceName" to entry.sourceName,
                "sourceType" to entry.sourceType,
                "isInvoiceSent" to entry.isInvoiceSent,
                "frequency" to entry.frequency,
                "contactName" to entry.contactName,
                "contactNumber" to entry.contactNumber,
            )

            val result = firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .collection(INCOME_LOGS_COLLECTION)
                .add(data)
                .awaitResult()

            if (result.isSuccess) {
                dao.markSynced(entry.localId, result.getOrThrow().id)
            } else {
                return WorkResult.retry()
            }
        }
        return pullRemoteIncomeLogs(uid, firestore, database)
    }

    private suspend fun syncIncomeSources(
        uid: String,
        firestore: FirebaseFirestore,
        database: IncomeDatabase,
    ): WorkResult {
        val sourceDao = database.incomeSourceDao()
        val pendingSources = sourceDao.getUnsynced(uid)
        for (source in pendingSources) {
            val result = if (source.isDeleted) {
                deleteRemoteSource(uid, firestore, source)
            } else {
                upsertRemoteSource(uid, firestore, source)
            }

            if (result.isSuccess) {
                sourceDao.markSynced(source.localId, result.getOrThrow())
            } else {
                return WorkResult.retry()
            }
        }
        return pullRemoteIncomeSources(uid, firestore, database)
    }

    private suspend fun syncScheduledIncomes(
        uid: String,
        firestore: FirebaseFirestore,
        database: IncomeDatabase,
    ): WorkResult {
        val scheduledDao = database.scheduledIncomeDao()
        val pendingScheduled = scheduledDao.getUnsynced(uid)
        for (scheduled in pendingScheduled) {
            val result = if (scheduled.isDeleted) {
                deleteRemoteScheduled(uid, firestore, scheduled)
            } else {
                upsertRemoteScheduled(uid, firestore, scheduled)
            }

            if (result.isSuccess) {
                scheduledDao.markSynced(scheduled.localId, result.getOrThrow())
            } else {
                return WorkResult.retry()
            }
        }

        return pullRemoteScheduledIncomes(uid, firestore, database)
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"
private const val INCOME_SOURCES_COLLECTION = "incomeSources"
private const val SCHEDULED_COLLECTION = "scheduledIncomes"

private suspend fun upsertRemoteSource(
    uid: String,
    firestore: FirebaseFirestore,
    source: IncomeSourceEntity,
): Result<String?> {
    val data = mapOf(
        "name" to source.name,
        "types" to source.typesCsv.split(',').filter { it.isNotBlank() },
        "createdAt" to source.createdAtEpochMillis,
        "updatedAt" to source.updatedAtEpochMillis,
        "userId" to uid,
    )

    return if (source.remoteId != null) {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .document(source.remoteId)
            .set(data)
            .awaitResult()
            .map { source.remoteId }
    } else {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .add(data)
            .awaitResult()
            .map { it.id }
    }
}

private suspend fun deleteRemoteSource(
    uid: String,
    firestore: FirebaseFirestore,
    source: IncomeSourceEntity,
): kotlin.Result<String?> {
    val remoteId = source.remoteId ?: return kotlin.Result.success(null)
    return firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(INCOME_SOURCES_COLLECTION)
        .document(remoteId)
        .delete()
        .awaitResult()
        .map { remoteId }
}

private suspend fun upsertRemoteScheduled(
    uid: String,
    firestore: FirebaseFirestore,
    scheduled: ScheduledIncomeEntity,
): kotlin.Result<String?> {
    if (scheduled.remoteId == null) {
        val ensureResult = ensureScheduledCollection(uid, firestore)
        if (ensureResult.isFailure) return kotlin.Result.failure(ensureResult.exceptionOrNull()!!)
    }
    val status = when {
        scheduled.type == "pending" && scheduled.lastGeneratedEpochMillis != null -> "received"
        scheduled.type == "pending" -> "pending"
        scheduled.type == "recurrent" -> "active"
        else -> "pending"
    }
    val receivedAt = if (scheduled.type == "pending") {
        scheduled.lastGeneratedEpochMillis
    } else {
        null
    }
    val data = mapOf(
        "title" to scheduled.title,
        "amount" to scheduled.amount,
        "currency" to scheduled.currency,
        "type" to scheduled.type,
        "frequency" to scheduled.frequency,
        "scheduledDate" to scheduled.scheduledDateEpochMillis,
        "lastGenerated" to scheduled.lastGeneratedEpochMillis,
        "receivedAt" to receivedAt,
        "sourceId" to scheduled.sourceId,
        "sourceName" to scheduled.sourceName,
        "isInvoiceSent" to scheduled.isInvoiceSent,
        "contactName" to scheduled.contactName,
        "contactNumber" to scheduled.contactNumber,
        "status" to status,
        "updatedAt" to System.currentTimeMillis(),
        "userId" to uid,
    )

    return if (scheduled.remoteId != null) {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .document(scheduled.remoteId)
            .set(data)
            .awaitResult()
            .map { scheduled.remoteId }
    } else {
        firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .add(data)
            .awaitResult()
            .map { it.id }
    }
}

private suspend fun ensureScheduledCollection(
    uid: String,
    firestore: FirebaseFirestore,
): kotlin.Result<Unit> {
    val now = System.currentTimeMillis()
    val userResult = firestore.collection(USERS_COLLECTION)
        .document(uid)
        .set(mapOf("updatedAt" to now), SetOptions.merge())
        .awaitResult()
    if (userResult.isFailure) return kotlin.Result.failure(userResult.exceptionOrNull()!!)

    return firestore.collection(USERS_COLLECTION)
        .document(uid)
        .collection(SCHEDULED_COLLECTION)
        .document("_meta")
        .set(mapOf("createdAt" to now), SetOptions.merge())
        .awaitResult()
        .map { }
}

private suspend fun deleteRemoteScheduled(
    uid: String,
    firestore: FirebaseFirestore,
    scheduled: ScheduledIncomeEntity,
): kotlin.Result<String?> {
    val remoteId = scheduled.remoteId ?: return kotlin.Result.success(null)
    return firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(SCHEDULED_COLLECTION)
        .document(remoteId)
        .delete()
        .awaitResult()
        .map { remoteId }
}

private suspend fun pullRemoteIncomeLogs(
    uid: String,
    firestore: FirebaseFirestore,
    database: IncomeDatabase,
): WorkResult {
    val logDao = database.incomeLogDao()
    val remoteResult = firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(INCOME_LOGS_COLLECTION)
        .get()
        .awaitResult()

    if (remoteResult.isFailure) return WorkResult.retry()
    val snapshot = remoteResult.getOrThrow()

    for (doc in snapshot.documents) {
        val remoteId = doc.id
        val existing = logDao.getByRemoteId(remoteId)
        if (existing != null) continue

        val title = doc.getString("title") ?: continue
        val amount = parseDouble(doc.get("amount")) ?: continue
        val currency = doc.getString("currency") ?: continue
        val receivedAt = parseLong(doc.get("receivedAt")) ?: continue
        val createdAt = parseLong(doc.get("createdAt")) ?: receivedAt

        val entity = IncomeLogEntity(
            userId = uid,
            title = title,
            amount = amount,
            currency = currency,
            receivedAtEpochMillis = receivedAt,
            createdAtEpochMillis = createdAt,
            sourceId = parseLong(doc.get("sourceId")),
            sourceName = doc.getString("sourceName"),
            sourceType = doc.getString("sourceType"),
            isInvoiceSent = doc.getBoolean("isInvoiceSent") ?: false,
            frequency = doc.getString("frequency"),
            contactName = doc.getString("contactName"),
            contactNumber = doc.getString("contactNumber"),
            remoteId = remoteId,
            isSynced = true,
        )
        logDao.insert(entity)
    }

    return WorkResult.success()
}

private suspend fun pullRemoteIncomeSources(
    uid: String,
    firestore: FirebaseFirestore,
    database: IncomeDatabase,
): WorkResult {
    val sourceDao = database.incomeSourceDao()
    val remoteResult = firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(INCOME_SOURCES_COLLECTION)
        .get()
        .awaitResult()

    if (remoteResult.isFailure) return WorkResult.retry()
    val snapshot = remoteResult.getOrThrow()

    val remoteIds = mutableSetOf<String>()
    for (doc in snapshot.documents) {
        val remoteId = doc.id
        remoteIds.add(remoteId)

        val existing = sourceDao.getByRemoteId(remoteId)
        if (existing?.isDeleted == true && !existing.isSynced) continue

        val name = doc.getString("name") ?: continue
        val typesList = parseStringList(doc.get("types"))
        val createdAt = parseLong(doc.get("createdAt")) ?: System.currentTimeMillis()
        val updatedAt = parseLong(doc.get("updatedAt")) ?: createdAt

        val entity = IncomeSourceEntity(
            localId = existing?.localId ?: 0L,
            userId = uid,
            name = name,
            typesCsv = typesList.joinToString(","),
            createdAtEpochMillis = createdAt,
            updatedAtEpochMillis = updatedAt,
            remoteId = remoteId,
            isSynced = true,
            isDeleted = false,
        )
        sourceDao.upsert(entity)
    }

    val syncedRemoteIds = sourceDao.getSyncedRemoteIds(uid)
    val missingRemoteIds = syncedRemoteIds.filter { it !in remoteIds }
    if (missingRemoteIds.isNotEmpty()) {
        sourceDao.markDeletedByRemoteIds(uid, missingRemoteIds)
    }

    return WorkResult.success()
}

private suspend fun pullRemoteScheduledIncomes(
    uid: String,
    firestore: FirebaseFirestore,
    database: IncomeDatabase,
): WorkResult {
    val scheduledDao = database.scheduledIncomeDao()
    val remoteResult = firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(SCHEDULED_COLLECTION)
        .get()
        .awaitResult()

    if (remoteResult.isFailure) return WorkResult.retry()
    val snapshot = remoteResult.getOrThrow()

    val remoteIds = mutableSetOf<String>()
    for (doc in snapshot.documents) {
        val remoteId = doc.id
        remoteIds.add(remoteId)

        val existing = scheduledDao.getByRemoteId(remoteId)
        if (existing?.isDeleted == true && !existing.isSynced) continue

        val title = doc.getString("title") ?: continue
        val amount = parseDouble(doc.get("amount")) ?: continue
        val currency = doc.getString("currency") ?: continue
        val type = doc.getString("type") ?: continue
        val scheduledDate = parseLong(doc.get("scheduledDate")) ?: continue

        val lastGenerated = parseLong(doc.get("lastGenerated")) ?: parseLong(doc.get("receivedAt"))
        val entity = ScheduledIncomeEntity(
            localId = existing?.localId ?: 0L,
            userId = uid,
            title = title,
            amount = amount,
            currency = currency,
            type = type,
            frequency = doc.getString("frequency"),
            scheduledDateEpochMillis = scheduledDate,
            lastGeneratedEpochMillis = lastGenerated,
            sourceId = parseLong(doc.get("sourceId")),
            sourceName = doc.getString("sourceName"),
            isInvoiceSent = doc.getBoolean("isInvoiceSent") ?: false,
            contactName = doc.getString("contactName"),
            contactNumber = doc.getString("contactNumber"),
            remoteId = remoteId,
            isSynced = true,
            isDeleted = false,
        )
        scheduledDao.upsert(entity)
    }

    val syncedRemoteIds = scheduledDao.getSyncedRemoteIds(uid)
    val missingRemoteIds = syncedRemoteIds.filter { it !in remoteIds }
    if (missingRemoteIds.isNotEmpty()) {
        scheduledDao.markDeletedByRemoteIds(uid, missingRemoteIds)
    }

    return WorkResult.success()
}

private fun parseLong(value: Any?): Long? = when (value) {
    is Long -> value
    is Int -> value.toLong()
    is Double -> value.toLong()
    is Float -> value.toLong()
    else -> null
}

private fun parseDouble(value: Any?): Double? = when (value) {
    is Double -> value
    is Long -> value.toDouble()
    is Int -> value.toDouble()
    is Float -> value.toDouble()
    else -> null
}

private fun parseStringList(value: Any?): List<String> {
    val list = value as? List<*> ?: return emptyList()
    return list.mapNotNull { it as? String }.filter { it.isNotBlank() }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): kotlin.Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(kotlin.Result.success(kotlin.Result.success(task.result)))
            } else {
                continuation.resumeWith(
                    kotlin.Result.success(
                        kotlin.Result.failure(task.exception ?: Exception("Unknown error"))
                    )
                )
            }
        }
    }
}
