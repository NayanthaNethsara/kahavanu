package com.kahavanu.data.income

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.sync.IncomeSyncScheduler
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultIncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val incomeLogDao: IncomeLogDao,
    private val syncScheduler: IncomeSyncScheduler,
) : IncomeRepository {
    init {
        syncScheduler.enqueue()
    }

    override fun observeIncomeLogs(): Flow<List<IncomeLogEntry>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return incomeLogDao.observeLogs(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun logIncome(entry: IncomeLogEntry): Result<IncomeLogResult> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val createdAt = System.currentTimeMillis()
        val localId = incomeLogDao.insert(entry.toEntity(uid, createdAt))

        val data = mapOf(
            "title" to entry.title,
            "amount" to entry.amount,
            "currency" to entry.currency,
            "note" to entry.note,
            "receivedAt" to entry.receivedAtEpochMillis,
            "createdAt" to createdAt,
            "userId" to uid,
        )

        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .add(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            incomeLogDao.markSynced(localId, remoteResult.getOrThrow().id)
            Result.success(IncomeLogResult.SYNCED)
        } else {
            syncScheduler.enqueue()
            Result.success(IncomeLogResult.LOCAL_ONLY)
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
