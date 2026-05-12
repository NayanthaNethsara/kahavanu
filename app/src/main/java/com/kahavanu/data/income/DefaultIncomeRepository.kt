package com.kahavanu.data.income

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeLogEntity
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
import android.util.Log
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.data.income.generateClientId
import com.kahavanu.data.income.logKey
import com.kahavanu.data.income.normalizeTypesCsv
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

    private var incomeLogsListener: ListenerRegistration? = null
    private var incomeSourcesListener: ListenerRegistration? = null
    private var scheduledIncomeListener: ListenerRegistration? = null
    private var listenerUserId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            stopRealtimeListeners()
            return@AuthStateListener
        }
        repositoryScope.launch {
            dedupeLocalLogs(uid)
            dedupeLocalSources(uid)
        }
        startRealtimeListeners(uid)
        syncScheduler.enqueue()
    }

    init {
        syncScheduler.enqueue()
        syncScheduler.scheduleIncomeProcessing()
        repositoryScope.launch {
            auth.currentUser?.uid?.let { uid ->
                dedupeLocalLogs(uid)
                dedupeLocalSources(uid)
            }
            processScheduledIncomes()
        }
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { startRealtimeListeners(it) }
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
        val localEntity = entry.toEntity(uid, createdAt)
        val localId = incomeLogDao.insert(localEntity)

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
            "clientId" to localEntity.clientId,
            "updatedAt" to System.currentTimeMillis(),
        )

        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .document(localEntity.clientId)
            .set(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            incomeLogDao.markSynced(localId, localEntity.clientId)
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

    private suspend fun dedupeLocalLogs(uid: String) {
        val logs = incomeLogDao.getLogsForUser(uid)
        if (logs.size < 2) return

        val keepByKey = LinkedHashMap<String, IncomeLogEntity>()
        val duplicates = mutableListOf<Long>()

        for (log in logs) {
            val key = log.logKey()
            val existing = keepByKey[key]
            if (existing == null) {
                keepByKey[key] = log
            } else {
                val keep = when {
                    existing.remoteId != null && log.remoteId == null -> existing
                    existing.remoteId == null && log.remoteId != null -> log
                    existing.updatedAtEpochMillis >= log.updatedAtEpochMillis -> existing
                    else -> log
                }
                val drop = if (keep === existing) log else existing
                keepByKey[key] = keep
                duplicates.add(drop.localId)
            }
        }

        if (duplicates.isNotEmpty()) {
            incomeLogDao.deleteByLocalIds(duplicates)
        }
    }

    private suspend fun dedupeLocalSources(uid: String) {
        val sources = incomeSourceDao.getActiveSources(uid)
        if (sources.size < 2) return

        val keepByKey = LinkedHashMap<String, IncomeSourceEntity>()
        val duplicates = mutableListOf<Long>()

        for (source in sources) {
            val key = source.name.trim().lowercase() + "|" + normalizeTypesCsv(source.typesCsv)
            val existing = keepByKey[key]
            if (existing == null) {
                keepByKey[key] = source
            } else {
                val keep = if (existing.updatedAtEpochMillis >= source.updatedAtEpochMillis) existing else source
                val drop = if (keep === existing) source else existing
                keepByKey[key] = keep
                duplicates.add(drop.localId)
            }
        }

        if (duplicates.isNotEmpty()) {
            incomeSourceDao.deleteByLocalIds(duplicates)
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
                typesCsv = normalizeTypesCsv(seed.types.map { it.id }),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                clientId = generateClientId(),
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
            typesCsv = normalizeTypesCsv(source.types.map { it.id }),
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
            clientId = existing?.clientId ?: generateClientId(),
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

        val existing = if (scheduled.id != 0L) scheduledIncomeDao.getById(scheduled.id) else null
        val entity = scheduled.toEntity(uid, clientId = existing?.clientId ?: generateClientId())
        val localId = scheduledIncomeDao.upsert(entity)
        val saved = scheduledIncomeDao.getById(localId) ?: return Result.failure(Exception("Failed to save locally"))

        val remoteResult = upsertRemoteScheduled(uid, saved)
        return if (remoteResult.isSuccess) {
            scheduledIncomeDao.upsert(saved.copy(remoteId = remoteResult.getOrThrow(), isSynced = true))
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
            var nextDate = item.scheduledDateEpochMillis
            var lastGenerated = item.lastGeneratedEpochMillis ?: item.scheduledDateEpochMillis
            var generatedCount = 0

            while (nextDate <= now) {
                val logEntry = IncomeLogEntry(
                    title = item.title,
                    amount = item.amount,
                    currency = item.currency,
                    receivedAtEpochMillis = nextDate,
                    sourceId = item.sourceId,
                    sourceName = item.sourceName,
                    sourceType = IncomeSourceType.RECURRENT.id,
                    isInvoiceSent = item.isInvoiceSent,
                    frequency = item.frequency,
                    contactName = item.contactName,
                    contactNumber = item.contactNumber
                )
                logIncome(logEntry)
                generatedCount += 1
                lastGenerated = nextDate

                val computedNext = calculateNextScheduledDate(nextDate, item.frequency)
                if (computedNext <= nextDate) break
                nextDate = computedNext
            }

            if (generatedCount > 0) {
                val updated = item.copy(
                    scheduledDateEpochMillis = nextDate,
                    lastGeneratedEpochMillis = lastGenerated,
                    occurrenceCount = item.occurrenceCount + generatedCount,
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

    private fun startRealtimeListeners(uid: String) {
        if (listenerUserId == uid && incomeLogsListener != null && incomeSourcesListener != null && scheduledIncomeListener != null) {
            return
        }
        stopRealtimeListeners()
        listenerUserId = uid

        incomeLogsListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_LOGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("IncomeRepository", "Income logs listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (change in snapshot.documentChanges) {
                        handleIncomeLogChange(uid, change)
                    }
                }
            }

        incomeSourcesListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("IncomeRepository", "Income sources listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (change in snapshot.documentChanges) {
                        handleIncomeSourceChange(uid, change)
                    }
                }
            }

        scheduledIncomeListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("IncomeRepository", "Scheduled income listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (change in snapshot.documentChanges) {
                        handleScheduledIncomeChange(uid, change)
                    }
                }
            }
    }

    private fun stopRealtimeListeners() {
        incomeLogsListener?.remove()
        incomeSourcesListener?.remove()
        scheduledIncomeListener?.remove()
        incomeLogsListener = null
        incomeSourcesListener = null
        scheduledIncomeListener = null
        listenerUserId = null
    }

    private suspend fun handleIncomeLogChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remoteId = doc.id
        val clientId = doc.getString("clientId") ?: remoteId
        when (change.type) {
            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                val localByRemote = incomeLogDao.getByRemoteId(remoteId)
                val localByClientId = if (localByRemote == null) {
                    incomeLogDao.getByClientId(clientId)
                } else {
                    null
                }
                val remoteEntity = doc.toIncomeLogEntity(uid, remoteId, 0L)
                val localByKey = if (localByRemote == null && localByClientId == null) {
                    incomeLogDao.getUnsynced(uid)
                        .firstOrNull { it.logKey() == remoteEntity.logKey() }
                } else {
                    null
                }
                val local = localByRemote ?: localByClientId ?: localByKey
                val entity = remoteEntity.copy(localId = local?.localId ?: 0L)
                if (local == null || entity.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                    incomeLogDao.upsert(entity)
                }
            }
            DocumentChange.Type.REMOVED -> {
                incomeLogDao.deleteByRemoteId(remoteId)
            }
        }
    }

    private suspend fun handleIncomeSourceChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remoteId = doc.id
        val clientId = doc.getString("clientId") ?: remoteId
        when (change.type) {
            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                val remoteEntity = doc.toIncomeSourceEntity(uid, remoteId, 0L)
                val localByRemote = incomeSourceDao.getByRemoteId(remoteId)
                val localByClientId = if (localByRemote == null) {
                    incomeSourceDao.getByClientId(clientId)
                } else {
                    null
                }
                val localByKey = if (localByRemote == null && localByClientId == null) {
                    incomeSourceDao.getActiveSources(uid).firstOrNull {
                        it.name.equals(remoteEntity.name, ignoreCase = true) &&
                            normalizeTypesCsv(it.typesCsv) == normalizeTypesCsv(remoteEntity.typesCsv)
                    }
                } else {
                    null
                }
                val local = localByRemote ?: localByClientId ?: localByKey
                val entity = remoteEntity.copy(localId = local?.localId ?: 0L)
                if (local == null || entity.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                    incomeSourceDao.upsert(entity)
                }
            }
            DocumentChange.Type.REMOVED -> {
                incomeSourceDao.markDeletedByRemoteIds(uid, listOf(remoteId))
            }
        }
    }

    private suspend fun handleScheduledIncomeChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remoteId = doc.id
        val clientId = doc.getString("clientId") ?: remoteId
        when (change.type) {
            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                val localByRemote = scheduledIncomeDao.getByRemoteId(remoteId)
                val localByClientId = if (localByRemote == null) {
                    scheduledIncomeDao.getByClientId(clientId)
                } else {
                    null
                }
                val local = localByRemote ?: localByClientId
                val entity = doc.toScheduledIncomeEntity(uid, remoteId, local?.localId ?: 0L)
                if (local == null || entity.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                    scheduledIncomeDao.upsert(entity)
                }
            }
            DocumentChange.Type.REMOVED -> {
                scheduledIncomeDao.markDeletedByRemoteIds(uid, listOf(remoteId))
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
            "clientId" to source.clientId,
            "userId" to uid,
        )

        val docId = source.remoteId ?: source.clientId
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(INCOME_SOURCES_COLLECTION)
            .document(docId)
            .set(data)
            .awaitResult()
            .map { docId }
    }

    private suspend fun deleteRemoteSource(
        uid: String,
        source: IncomeSourceEntity,
    ): Result<String?> {
        val remoteId = source.remoteId ?: source.clientId
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
            "clientId" to scheduled.clientId,
            "occurrenceCount" to scheduled.occurrenceCount,
            "userId" to uid,
        )

        val docId = scheduled.remoteId ?: scheduled.clientId
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SCHEDULED_COLLECTION)
            .document(docId)
            .set(data)
            .awaitResult()
            .map { docId }
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
        val remoteId = scheduled.remoteId ?: scheduled.clientId
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




