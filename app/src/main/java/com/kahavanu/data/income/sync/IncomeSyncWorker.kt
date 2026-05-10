package com.kahavanu.data.income.sync

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.local.IncomeDatabase
import com.kahavanu.data.income.local.IncomeDatabaseMigrations
import com.kahavanu.data.income.local.IncomeSourceEntity
import kotlinx.coroutines.suspendCancellableCoroutine

class IncomeSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
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
            if (logResult is Result.Retry) {
                Result.retry()
            } else {
                syncIncomeSources(uid, firestore, database)
            }
        } catch (_: Exception) {
            Result.retry()
        } finally {
            database.close()
        }
    }

    private suspend fun syncIncomeLogs(
        uid: String,
        firestore: FirebaseFirestore,
        database: IncomeDatabase,
    ): Result {
        val dao = database.incomeLogDao()
        val unsynced = dao.getUnsynced(uid)
        if (unsynced.isEmpty()) {
            return Result.success()
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
                return Result.retry()
            }
        }
        return Result.success()
    }

    private suspend fun syncIncomeSources(
        uid: String,
        firestore: FirebaseFirestore,
        database: IncomeDatabase,
    ): Result {
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
                return Result.retry()
            }
        }
        return Result.success()
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"
private const val INCOME_SOURCES_COLLECTION = "incomeSources"

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
): Result<String?> {
    val remoteId = source.remoteId ?: return Result.success(null)
    return firestore
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(INCOME_SOURCES_COLLECTION)
        .document(remoteId)
        .delete()
        .awaitResult()
        .map { remoteId }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(Result.success(Result.success(task.result)))
            } else {
                continuation.resumeWith(
                    Result.success(
                        Result.failure(task.exception ?: Exception("Unknown error"))
                    )
                )
            }
        }
    }
}
