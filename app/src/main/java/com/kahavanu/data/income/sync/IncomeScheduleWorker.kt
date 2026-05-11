package com.kahavanu.data.income.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kahavanu.domain.repository.IncomeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class IncomeScheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: IncomeRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = repository.processScheduledIncomes()
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
