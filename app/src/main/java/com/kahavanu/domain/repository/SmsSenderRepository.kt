package com.kahavanu.domain.repository

import com.kahavanu.domain.model.SmsSender
import kotlinx.coroutines.flow.Flow

interface SmsSenderRepository {
    fun observeAuthorizedSenders(): Flow<List<SmsSender>>
    suspend fun addAuthorizedSender(senderName: String): Result<Unit>
    suspend fun toggleSenderEnabled(senderId: String, isEnabled: Boolean): Result<Unit>
    suspend fun deleteAuthorizedSender(senderId: String): Result<Unit>
}
