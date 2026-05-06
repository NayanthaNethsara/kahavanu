package com.kahavanu.data.income.sync

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.local.IncomeDatabase
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
        ).build()

        return try {
            val dao = database.incomeLogDao()
            val unsynced = dao.getUnsynced(uid)
            if (unsynced.isEmpty()) {
                Result.success()
            } else {
                val firestore = FirebaseFirestore.getInstance()
                for (entry in unsynced) {
                    val data = mapOf(
                        "title" to entry.title,
                        "amount" to entry.amount,
                        "currency" to entry.currency,
                        "note" to entry.note,
                        "receivedAt" to entry.receivedAtEpochMillis,
                        "createdAt" to entry.createdAtEpochMillis,
                        "userId" to uid,
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
                Result.success()
            }
        } catch (_: Exception) {
            Result.retry()
        } finally {
            database.close()
        }
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(kotlin.Result.success(Result.success(task.result)))
            } else {
                continuation.resumeWith(
                    kotlin.Result.success(
                        Result.failure(task.exception ?: Exception("Unknown error"))
                    )
                )
            }
        }
    }
}
