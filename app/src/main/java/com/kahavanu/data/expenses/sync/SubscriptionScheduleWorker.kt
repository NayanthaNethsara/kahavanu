package com.kahavanu.data.expenses.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kahavanu.domain.repository.ExpensesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SubscriptionScheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ExpensesRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = repository.processSubscriptions()
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
