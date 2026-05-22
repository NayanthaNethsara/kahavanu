package com.kahavanu.domain.repository

import kotlinx.coroutines.flow.Flow

interface SmsScanRepository {
    val isScanningFlow: Flow<Boolean>
    fun scanNow()
}
