package com.kahavanu.data.expenses

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.expenses.toEntity
import com.kahavanu.data.expenses.toDomain
import com.kahavanu.data.expenses.local.SubscriptionDao
import com.kahavanu.data.expenses.local.SubscriptionEntity
import com.kahavanu.data.expenses.sync.ExpensesSyncScheduler
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.model.Subscription
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SubscriptionsRepository
import com.kahavanu.notifications.AppNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSubscriptionsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val subscriptionDao: SubscriptionDao,
    private val expensesRepository: ExpensesRepository,
    private val syncScheduler: ExpensesSyncScheduler,
    private val appNotifier: AppNotifier,
) : SubscriptionsRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            repositoryScope.launch {
                processSubscriptions()
            }
            syncScheduler.enqueue()
        }
    }

    init {
        syncScheduler.scheduleSubscriptionProcessing()
        auth.addAuthStateListener(authStateListener)
        repositoryScope.launch {
            processSubscriptions()
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
        val entity = subscription.toEntity(
            userId = uid,
            defaultClientId = existing?.clientId,
            localId = existing?.localId ?: 0L,
            lastGeneratedEpochMillis = existing?.lastGeneratedEpochMillis,
            occurrenceCount = existing?.occurrenceCount ?: 0
        )
        val localId = subscriptionDao.upsert(entity)
        val saved = subscriptionDao.getById(localId) ?: return Result.failure(Exception("Failed to save locally"))

        // Re-process subscriptions right away to check if due
        repositoryScope.launch {
            processSubscriptions()
        }

        val remoteResult = upsertRemoteSubscription(uid, saved)
        return if (remoteResult.isSuccess) {
            subscriptionDao.upsert(saved.copy(remoteId = remoteResult.getOrThrow(), isSynced = true))
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
    }

    override suspend fun deleteSubscription(id: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val existing = subscriptionDao.getByClientId(id) ?: return Result.success(Unit)
        subscriptionDao.markDeleted(id)

        val remoteResult = deleteRemoteSubscription(uid, existing)
        return if (remoteResult.isSuccess) {
            subscriptionDao.markSynced(existing.localId, existing.remoteId ?: "")
            Result.success(Unit)
        } else {
            syncScheduler.enqueue()
            Result.success(Unit)
        }
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
                expensesRepository.logExpense(logEntry)
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
                appNotifier.notifySubscriptionCharged(item.title, item.amount, item.currency)
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

    private suspend fun upsertRemoteSubscription(
        uid: String,
        subscription: SubscriptionEntity,
    ): Result<String?> {
        return try {
            val data = subscription.toFirestoreMap()
            val docId = subscription.clientId
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(SUBSCRIPTIONS_COLLECTION)
                .document(docId)
                .set(data)
                .awaitResult()
                .map { docId }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteRemoteSubscription(
        uid: String,
        subscription: SubscriptionEntity,
    ): Result<Unit> {
        return try {
            val docId = subscription.clientId
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .collection(SUBSCRIPTIONS_COLLECTION)
                .document(docId)
                .delete()
                .awaitResult()
                .map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private const val USERS_COLLECTION = "users"
private const val SUBSCRIPTIONS_COLLECTION = "subscriptions"
