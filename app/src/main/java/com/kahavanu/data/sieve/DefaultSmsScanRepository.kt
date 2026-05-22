package com.kahavanu.data.sieve

import com.kahavanu.data.sieve.sms.SmsScanScheduler
import com.kahavanu.domain.repository.SmsScanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSmsScanRepository @Inject constructor(
    private val scheduler: SmsScanScheduler,
) : SmsScanRepository {
    override val isScanningFlow: Flow<Boolean> = scheduler.isScanningFlow
    override fun scanNow() = scheduler.enqueue()
}
