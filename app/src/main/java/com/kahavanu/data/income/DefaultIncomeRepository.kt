package com.kahavanu.data.income

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.repository.IncomeRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultIncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : IncomeRepository {
    override suspend fun logIncome(entry: IncomeLogEntry): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val data = mapOf(
            "title" to entry.title,
            "amount" to entry.amount,
            "currency" to entry.currency,
            "note" to entry.note,
            "receivedAt" to entry.receivedAtEpochMillis,
            "createdAt" to System.currentTimeMillis(),
            "userId" to uid,
        )

        return firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .add(data)
            .awaitUnitResult()
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"

private suspend fun com.google.android.gms.tasks.Task<*>.awaitUnitResult(): Result<Unit> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(kotlin.Result.success(Result.success(Unit)))
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
