package com.kahavanu.data.income

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.ScheduledIncomeDao
import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.data.income.sync.IncomeSyncScheduler
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.repository.IncomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.kahavanu.domain.model.ScheduledIncome
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class DefaultIncomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val incomeLogDao: IncomeLogDao,
    private val incomeSourceDao: IncomeSourceDao,
    private val scheduledIncomeDao: ScheduledIncomeDao,
    private val syncScheduler: IncomeSyncScheduler,
    @ApplicationContext private val appContext: Context,
) : IncomeRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        syncScheduler.enqueue()
        syncScheduler.scheduleIncomeProcessing()
        repositoryScope.launch {
            processScheduledIncomes()
        }
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

        // If offline, persist locally and enqueue sync without attempting network write.
        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(IncomeLogResult.LOCAL_ONLY)
        }

        val data = mapOf(
            "title" to entry.title,
            "amount" to entry.amount,
            "currency" to entry.currency,
            "receivedAt" to entry.receivedAtEpochMillis,
            "createdAt" to createdAt,
            "userId" to uid,
            "sourceId" to entry.sourceId,
            "sourceName" to entry.sourceName,
            "sourceType" to entry.sourceType,
            "isInvoiceSent" to entry.isInvoiceSent,
            "frequency" to entry.frequency,
            "contactName" to entry.contactName,
            "contactNumber" to entry.contactNumber,
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

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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


    override fun observeScheduledIncomes(): Flow<List<ScheduledIncome>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return scheduledIncomeDao.observeScheduled(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun upsertScheduledIncome(scheduled: ScheduledIncome): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val localId = scheduledIncomeDao.upsert(scheduled.toEntity(uid))
        val existing = scheduledIncomeDao.getById(localId) ?: return Result.failure(Exception("Failed to save locally"))

        val remoteResult = upsertRemoteScheduled(uid, existing)
        return if (remoteResult.isSuccess) {
            scheduledIncomeDao.upsert(existing.copy(remoteId = remoteResult.getOrThrow(), isSynced = true))
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun deleteScheduledIncome(id: Long): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = scheduledIncomeDao.getById(id) ?: return Result.success(Unit)
        scheduledIncomeDao.markDeleted(id)

        val remoteResult = deleteRemoteScheduled(uid, existing)
        return if (remoteResult.isSuccess) {
            scheduledIncomeDao.markSynced(id, existing.remoteId)
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun markScheduledAsReceived(id: Long): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val scheduled = scheduledIncomeDao.getById(id) ?: return Result.failure(Exception("Scheduled item not found"))
        
        // Log as actual income
        val entry = IncomeLogEntry(
            title = scheduled.title,
            amount = scheduled.amount,
            currency = scheduled.currency,
            receivedAtEpochMillis = System.currentTimeMillis(),
            sourceId = scheduled.sourceId,
            sourceName = scheduled.sourceName,
            sourceType = IncomeSourceType.ONE_TIME.id,
            isInvoiceSent = scheduled.isInvoiceSent,
            contactName = scheduled.contactName,
            contactNumber = scheduled.contactNumber
        )
        
        val logResult = logIncome(entry)
        if (logResult.isSuccess) {
            if (scheduled.type == IncomeSourceType.PENDING.id) {
                val updated = scheduled.copy(
                    lastGeneratedEpochMillis = System.currentTimeMillis(),
                    isSynced = false
                )
                scheduledIncomeDao.upsert(updated)
                val remoteResult = upsertRemoteScheduled(uid, updated)
                if (remoteResult.isSuccess) {
                    scheduledIncomeDao.markSynced(updated.localId, remoteResult.getOrThrow())
                } else {
                    syncScheduler.enqueue()
                }
            }
            return Result.success(Unit)
        }
        return Result.failure(Exception("Failed to log income"))
    }

    override suspend fun processScheduledIncomes(): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val now = System.currentTimeMillis()
        val dueItems = scheduledIncomeDao.getDueScheduled(uid, now)
        
        dueItems.filter { it.type == IncomeSourceType.RECURRENT.id }.forEach { item ->
            // Generate Log
            val logEntry = IncomeLogEntry(
                title = item.title,
                amount = item.amount,
                currency = item.currency,
                receivedAtEpochMillis = item.scheduledDateEpochMillis,
                sourceId = item.sourceId,
                sourceName = item.sourceName,
                sourceType = IncomeSourceType.RECURRENT.id,
                isInvoiceSent = item.isInvoiceSent,
                frequency = item.frequency,
                contactName = item.contactName,
                contactNumber = item.contactNumber
            )
            logIncome(logEntry)
            
            // Update Scheduled Item for next occurrence
            val nextDate = calculateNextScheduledDate(item.scheduledDateEpochMillis, item.frequency)
            val updated = item.copy(
                scheduledDateEpochMillis = nextDate,
                lastGeneratedEpochMillis = now,
                isSynced = false
            )
            scheduledIncomeDao.upsert(updated)
            val remoteResult = upsertRemoteScheduled(uid, updated)
            if (remoteResult.isSuccess) {
                scheduledIncomeDao.markSynced(updated.localId, remoteResult.getOrThrow())
            } else {
                syncScheduler.enqueue()
            }
        }
    }

    private fun calculateNextScheduledDate(currentDate: Long, frequency: String?): Long {
        val date = java.time.Instant.ofEpochMilli(currentDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            
        val nextDate = when (frequency?.lowercase()) {
            "daily" -> date.plusDays(1)
            "weekly" -> date.plusWeeks(1)
            "monthly" -> date.plusMonths(1)
            "yearly" -> date.plusYears(1)
            else -> date.plusMonths(1) // Default to monthly
        }
        return nextDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
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
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(INCOME_SOURCES_COLLECTION)
                .document(source.remoteId)
                .set(data)
                .awaitResult()
                .map { source.remoteId }
        } else {
            firestore.collection(USERS_COLLECTION)
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
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .document(remoteId)
            .delete()
            .awaitResult()
            .map { remoteId }
    }

    private suspend fun upsertRemoteScheduled(
        uid: String,
        scheduled: ScheduledIncomeEntity,
    ): Result<String?> {
        if (scheduled.remoteId == null) {
            val ensureResult = ensureScheduledCollection(uid)
            if (ensureResult.isFailure) return Result.failure(ensureResult.exceptionOrNull()!!)
        }
        val status = when {
            scheduled.type == IncomeSourceType.PENDING.id && scheduled.lastGeneratedEpochMillis != null -> "received"
            scheduled.type == IncomeSourceType.PENDING.id -> "pending"
            scheduled.type == IncomeSourceType.RECURRENT.id -> "active"
            else -> "pending"
        }
        val receivedAt = if (scheduled.type == IncomeSourceType.PENDING.id) {
            scheduled.lastGeneratedEpochMillis
        } else {
            null
        }
        val data = mapOf(
            "title" to scheduled.title,
            "amount" to scheduled.amount,
            "currency" to scheduled.currency,
            "type" to scheduled.type,
            "frequency" to scheduled.frequency,
            "scheduledDate" to scheduled.scheduledDateEpochMillis,
            "lastGenerated" to scheduled.lastGeneratedEpochMillis,
            "receivedAt" to receivedAt,
            "sourceId" to scheduled.sourceId,
            "sourceName" to scheduled.sourceName,
            "isInvoiceSent" to scheduled.isInvoiceSent,
            "contactName" to scheduled.contactName,
            "contactNumber" to scheduled.contactNumber,
            "status" to status,
            "updatedAt" to System.currentTimeMillis(),
            "userId" to uid,
        )

        return if (scheduled.remoteId != null) {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(SCHEDULED_COLLECTION)
                .document(scheduled.remoteId)
                .set(data)
                .awaitResult()
                .map { scheduled.remoteId }
        } else {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(SCHEDULED_COLLECTION)
                .add(data)
                .awaitResult()
                .map { it.id }
        }
    }

    private suspend fun ensureScheduledCollection(uid: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val userResult = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(mapOf("updatedAt" to now), SetOptions.merge())
            .awaitResult()
        if (userResult.isFailure) return Result.failure(userResult.exceptionOrNull()!!)

        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .document("_meta")
            .set(mapOf("createdAt" to now), SetOptions.merge())
            .awaitResult()
            .map { }
    }

    private suspend fun deleteRemoteScheduled(
        uid: String,
        scheduled: ScheduledIncomeEntity,
    ): Result<String?> {
        val remoteId = scheduled.remoteId ?: return Result.success(null)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .document(remoteId)
            .delete()
            .awaitResult()
            .map { remoteId }
    }
}

private const val USERS_COLLECTION = "users"
private const val INCOME_LOGS_COLLECTION = "incomeLogs"
private const val INCOME_SOURCES_COLLECTION = "incomeSources"
private const val SCHEDULED_COLLECTION = "scheduledIncomes"

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


