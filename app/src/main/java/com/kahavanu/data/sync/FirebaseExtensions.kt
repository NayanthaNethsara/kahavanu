package com.kahavanu.data.sync

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Extension functions to convert Firebase Tasks to suspend-friendly Result.
 */

suspend fun Task<Void>.awaitResultVoid(): Result<Unit> {
    return suspendCancellableCoroutine<Result<Unit>> { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(
                    Result.failure(task.exception ?: Exception("Unknown error"))
                )
            }
        }
    }
}

suspend fun Task<DocumentReference>.awaitResultDocRef(): Result<DocumentReference> {
    return suspendCancellableCoroutine<Result<DocumentReference>> { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resume(Result.success(task.result!!))
            } else {
                continuation.resume(
                    Result.failure(task.exception ?: Exception("Unknown error"))
                )
            }
        }
    }
}

suspend fun Task<QuerySnapshot>.awaitResultQuery(): Result<QuerySnapshot> {
    return suspendCancellableCoroutine<Result<QuerySnapshot>> { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resume(Result.success(task.result!!))
            } else {
                continuation.resume(
                    Result.failure(task.exception ?: Exception("Unknown error"))
                )
            }
        }
    }
}

