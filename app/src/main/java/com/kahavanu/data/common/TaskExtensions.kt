package com.kahavanu.data.common

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Task<T>.awaitResult(): Result<T> {
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
