package com.kahavanu.domain.repository

import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import kotlinx.coroutines.flow.Flow

interface SmsSuggestionRepository {
    fun observePendingSuggestions(): Flow<List<SmsSuggestion>>
    fun observePendingByKinds(kinds: List<SuggestionKind>): Flow<List<SmsSuggestion>>
    suspend fun insertIfNew(suggestion: SmsSuggestion): Long
    suspend fun getById(id: Long): SmsSuggestion?
    suspend fun confirm(id: Long)
    suspend fun dismiss(id: Long)
}
