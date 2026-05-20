package com.kahavanu.data.income.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeSyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun enqueue() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<IncomeSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Duration.ofSeconds(30)
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    fun scheduleIncomeProcessing() {
        val request = androidx.work.PeriodicWorkRequestBuilder<IncomeScheduleWorker>(
            java.time.Duration.ofHours(24)
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Duration.ofMinutes(15)
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SCHEDULE_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    companion object {
        private const val WORK_NAME = "income-sync"
        private const val SCHEDULE_WORK_NAME = "income-schedule"
    }
}
