package com.kahavanu.data.income.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that triggers the income sync process.
 * Uses IncomeSyncManager for bidirectional sync with Firebase.
 * Firebase is the source of truth.
 */
@HiltWorker
class IncomeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: IncomeSyncManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return syncManager.sync()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
}
