package com.kahavanu.data.sieve

import com.google.firebase.auth.FirebaseAuth
import com.kahavanu.data.sieve.local.SmsSuggestionDao
import com.kahavanu.data.sieve.local.SmsSuggestionEntity
import com.kahavanu.data.sieve.local.toEntity
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.model.SuggestionStatus
import com.kahavanu.domain.repository.SmsSuggestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSmsSuggestionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val dao: SmsSuggestionDao,
) : SmsSuggestionRepository {

    override fun observePendingSuggestions(): Flow<List<SmsSuggestion>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return dao.observePending(uid).map { it.map(SmsSuggestionEntity::toDomain) }
    }

    override fun observePendingByKinds(kinds: List<SuggestionKind>): Flow<List<SmsSuggestion>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return dao.observePendingByKinds(uid, kinds.map { it.name })
            .map { it.map(SmsSuggestionEntity::toDomain) }
    }

    override suspend fun insertIfNew(suggestion: SmsSuggestion): Long {
        return dao.insertIfNew(suggestion.toEntity())
    }

    override suspend fun getById(id: Long): SmsSuggestion? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun confirm(id: Long) {
        dao.updateStatus(id, SuggestionStatus.CONFIRMED.name, System.currentTimeMillis())
    }

    override suspend fun dismiss(id: Long) {
        dao.updateStatus(id, SuggestionStatus.DISMISSED.name, System.currentTimeMillis())
    }
}
