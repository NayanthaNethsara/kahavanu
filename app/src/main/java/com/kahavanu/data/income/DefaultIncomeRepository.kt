package com.kahavanu.data.income

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.sync.IncomeSyncScheduler
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultIncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val incomeLogDao: IncomeLogDao,
    private val incomeSourceDao: IncomeSourceDao,
    private val syncScheduler: IncomeSyncScheduler,
) : IncomeRepository {
    init {
        syncScheduler.enqueue()
    }

    override fun observeIncomeLogs(): Flow<List<IncomeLogEntry>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return incomeLogDao.observeLogs(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun logIncome(entry: IncomeLogEntry): Result<IncomeLogResult> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val createdAt = System.currentTimeMillis()
        val localId = incomeLogDao.insert(entry.toEntity(uid, createdAt))

        val data = mapOf(
            "title" to entry.title,
            "amount" to entry.amount,
            "currency" to entry.currency,
            "note" to entry.note,
            "receivedAt" to entry.receivedAtEpochMillis,
            "createdAt" to createdAt,
            "userId" to uid,
        )

        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .add(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            incomeLogDao.markSynced(localId, remoteResult.getOrThrow().id)
            Result.success(IncomeLogResult.SYNCED)
        } else {
            syncScheduler.enqueue()
            Result.success(IncomeLogResult.LOCAL_ONLY)
        }
    }

    override fun observeIncomeSources(): Flow<List<IncomeSource>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return incomeSourceDao.observeSources(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun ensureDefaultSources() {
        val uid = auth.currentUser?.uid ?: return
        val hasSources = incomeSourceDao.countActiveSources(uid) > 0
        if (hasSources) return

        val now = System.currentTimeMillis()
        defaultSourceSeeds().forEach { seed ->
            val entity = IncomeSourceEntity(
                userId = uid,
                name = seed.name,
                typesCsv = seed.types.joinToString(",") { it.id },
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                isSynced = false,
                isDeleted = false,
            )
            incomeSourceDao.upsert(entity)
        }
        syncScheduler.enqueue()
    }

    override suspend fun upsertIncomeSource(source: IncomeSource): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val trimmedName = source.name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Source name cannot be blank"))
        }
        if (source.types.isEmpty()) {
            return Result.failure(IllegalArgumentException("Select at least one income type"))
        }

        val now = System.currentTimeMillis()
        val existing = if (source.id != 0L) incomeSourceDao.getById(source.id) else null
        val entity = IncomeSourceEntity(
            localId = existing?.localId ?: 0L,
            userId = uid,
            name = trimmedName,
            typesCsv = source.types.joinToString(",") { it.id },
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            remoteId = existing?.remoteId,
            isSynced = false,
            isDeleted = false,
        )

        val localId = incomeSourceDao.upsert(entity)
        val resolvedLocalId = if (entity.localId == 0L) localId else entity.localId
        val updatedEntity = entity.copy(localId = resolvedLocalId)

        val remoteResult = upsertRemoteSource(uid, updatedEntity)
        return if (remoteResult.isSuccess) {
            incomeSourceDao.markSynced(updatedEntity.localId, remoteResult.getOrThrow())
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun deleteIncomeSource(sourceId: Long): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = incomeSourceDao.getById(sourceId)
            ?: return Result.success(Unit)

        val now = System.currentTimeMillis()
        incomeSourceDao.markDeleted(sourceId, now)

        val remoteResult = deleteRemoteSource(uid, existing)
        return if (remoteResult.isSuccess) {
            incomeSourceDao.markSynced(sourceId, remoteResult.getOrThrow())
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"
private const val INCOME_SOURCES_COLLECTION = "incomeSources"

private data class IncomeSourceSeed(
    val name: String,
    val types: Set<IncomeSourceType>,
)

private fun defaultSourceSeeds(): List<IncomeSourceSeed> = listOf(
    IncomeSourceSeed("Salary", setOf(IncomeSourceType.RECURRENT)),
    IncomeSourceSeed("Freelance", setOf(IncomeSourceType.ONE_TIME, IncomeSourceType.RECURRENT)),
    IncomeSourceSeed("AdSense", setOf(IncomeSourceType.RECURRENT)),
    IncomeSourceSeed("Crypto", setOf(IncomeSourceType.ONE_TIME, IncomeSourceType.PENDING)),
)

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resumeWith(kotlin.Result.success(Result.success(task.result)))
            } else {
                continuation.resumeWith(
                    kotlin.Result.success(
                        Result.failure(task.exception ?: Exception("Unknown error"))
                    )
                )
            }
        }
    }
}

private suspend fun upsertRemoteSource(
    uid: String,
    source: IncomeSourceEntity,
): Result<String?> {
    val data = mapOf(
        "name" to source.name,
        "types" to source.typesCsv.split(',').filter { it.isNotBlank() },
        "createdAt" to source.createdAtEpochMillis,
        "updatedAt" to source.updatedAtEpochMillis,
        "userId" to uid,
    )

    return if (source.remoteId != null) {
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .document(source.remoteId)
            .set(data)
            .awaitResult()
            .map { source.remoteId }
    } else {
        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .add(data)
            .awaitResult()
            .map { it.id }
    }
}

private suspend fun deleteRemoteSource(
    uid: String,
    source: IncomeSourceEntity,
): Result<String?> {
    val remoteId = source.remoteId ?: return Result.success(null)
    return FirebaseFirestore.getInstance()
        .collection(USERS_COLLECTION)
        .document(uid)
        .collection(INCOME_SOURCES_COLLECTION)
        .document(remoteId)
        .delete()
        .awaitResult()
        .map { remoteId }
}
