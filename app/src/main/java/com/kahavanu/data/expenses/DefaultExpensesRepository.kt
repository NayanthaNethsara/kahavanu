package com.kahavanu.data.expenses

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.expenses.toExpenseLogEntity
import com.kahavanu.data.expenses.toDomain
import com.kahavanu.data.expenses.toEntity
import com.kahavanu.data.expenses.local.ExpenseLogDao
import com.kahavanu.data.expenses.local.ExpenseLogEntity
import com.kahavanu.data.expenses.local.SubscriptionDao
import com.kahavanu.data.expenses.sync.ExpensesSyncScheduler
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.ExpenseLogResult
import com.kahavanu.domain.model.Subscription
import com.kahavanu.domain.repository.ExpensesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultExpensesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val expenseLogDao: ExpenseLogDao,
    private val subscriptionDao: SubscriptionDao,
    private val syncScheduler: ExpensesSyncScheduler,
    @ApplicationContext private val appContext: Context,
) : ExpensesRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private var expenseLogsListener: ListenerRegistration? = null
    private var listenerUserId: String? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            stopRealtimeListeners()
            return@AuthStateListener
        }
        repositoryScope.launch {
            dedupeLocalLogs(uid)
        }
        startRealtimeListeners(uid)
        syncScheduler.enqueue()
    }

    init {
        syncScheduler.enqueue()
        syncScheduler.scheduleSubscriptionProcessing()
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { startRealtimeListeners(it) }
        repositoryScope.launch {
            processSubscriptions()
        }
    }

    override fun observeExpenseLogs(): Flow<List<ExpenseLogEntry>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return expenseLogDao.observeLogs(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun logExpense(entry: ExpenseLogEntry): Result<ExpenseLogResult> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val createdAt = System.currentTimeMillis()
        val localEntity = entry.toEntity(uid, createdAt)
        val localId = expenseLogDao.insert(localEntity)

        if (!isOnline()) {
            syncScheduler.enqueue()
            return Result.success(ExpenseLogResult.LOCAL_ONLY)
        }

        val data = localEntity.toFirestoreMap()
        val remoteResult = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(EXPENSE_LOGS_COLLECTION)
            .document(localEntity.clientId)
            .set(data)
            .awaitResult()

        return if (remoteResult.isSuccess) {
            expenseLogDao.markSynced(localId, localEntity.clientId)
            Result.success(ExpenseLogResult.SYNCED)
        } else {
            syncScheduler.enqueue()
            Result.success(ExpenseLogResult.LOCAL_ONLY)
        }
    }

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun startRealtimeListeners(uid: String) {
        if (listenerUserId == uid && expenseLogsListener != null) {
            return
        }
        stopRealtimeListeners()
        listenerUserId = uid

        expenseLogsListener = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(EXPENSE_LOGS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ExpensesRepository", "Expense logs listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                repositoryScope.launch {
                    for (change in snapshot.documentChanges) {
                        handleExpenseLogChange(uid, change)
                    }
                }
            }
    }

    private fun stopRealtimeListeners() {
        expenseLogsListener?.remove()
        expenseLogsListener = null
        listenerUserId = null
    }

    private suspend fun handleExpenseLogChange(uid: String, change: DocumentChange) {
        val doc = change.document
        val remoteId = doc.id
        val clientId = doc.getString("clientId") ?: remoteId
        when (change.type) {
            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                val localByRemote = expenseLogDao.getByRemoteId(remoteId)
                val localByClientId = if (localByRemote == null) {
                    expenseLogDao.getByClientId(clientId)
                } else {
                    null
                }
                val remoteEntity = doc.toExpenseLogEntity(uid, remoteId, 0L)
                val localByKey = if (localByRemote == null && localByClientId == null) {
                    expenseLogDao.getUnsynced(uid)
                        .firstOrNull { it.logKey() == remoteEntity.logKey() }
                } else {
                    null
                }
                val local = localByRemote ?: localByClientId ?: localByKey
                val entity = remoteEntity.copy(localId = local?.localId ?: 0L)
                if (local == null || entity.updatedAtEpochMillis > local.updatedAtEpochMillis) {
                    expenseLogDao.upsert(entity)
                }
            }
            DocumentChange.Type.REMOVED -> {
                expenseLogDao.deleteByRemoteId(remoteId)
            }
        }
    }

    private suspend fun dedupeLocalLogs(uid: String) {
        val logs = expenseLogDao.getLogsForUser(uid)
        if (logs.size < 2) return

        val keepByKey = LinkedHashMap<String, ExpenseLogEntity>()
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
            expenseLogDao.deleteByLocalIds(duplicates)
        }
    }

    override fun observeSubscriptions(): Flow<List<Subscription>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return subscriptionDao.observeActiveSubscriptions(uid)
            .map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun upsertSubscription(subscription: Subscription): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = subscriptionDao.getByClientId(subscription.id)
        val entity = subscription.toEntity(uid, existing?.clientId)
        subscriptionDao.upsert(entity)

        // Re-process subscriptions right away to check if due
        repositoryScope.launch {
            processSubscriptions()
        }

        return Result.success(Unit)
    }

    override suspend fun deleteSubscription(id: String): Result<Unit> {
        subscriptionDao.markDeleted(id)
        return Result.success(Unit)
    }

    override suspend fun processSubscriptions(): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val now = System.currentTimeMillis()
        val dueItems = subscriptionDao.getDueSubscriptions(uid, now)

        dueItems.forEach { item ->
            var nextDate = item.scheduledDateEpochMillis
            var lastGenerated = item.lastGeneratedEpochMillis ?: item.scheduledDateEpochMillis
            var generatedCount = 0

            while (nextDate <= now) {
                val logEntry = ExpenseLogEntry(
                    title = item.title,
                    amount = item.amount,
                    currency = item.currency,
                    spentAtEpochMillis = nextDate,
                    category = item.category,
                    merchant = "Subscription",
                )
                logExpense(logEntry)
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
                subscriptionDao.upsert(updated)
            }
        }
    }

    private fun calculateNextScheduledDate(currentDate: Long, frequency: String): Long {
        val date = java.time.Instant.ofEpochMilli(currentDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        val nextDate = when (frequency.lowercase()) {
            "daily" -> date.plusDays(1)
            "weekly" -> date.plusWeeks(1)
            "monthly" -> date.plusMonths(1)
            "yearly" -> date.plusYears(1)
            else -> date.plusMonths(1)
        }
        return nextDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

private const val USERS_COLLECTION = "users"
private const val EXPENSE_LOGS_COLLECTION = "expenseLogs"
