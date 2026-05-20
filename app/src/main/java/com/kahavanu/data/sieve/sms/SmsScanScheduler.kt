package com.kahavanu.data.sieve.sms

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsScanScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueue() {
        val request = OneTimeWorkRequestBuilder<SmsScanWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    companion object {
        const val WORK_NAME = "sms-scan"
    }
}
