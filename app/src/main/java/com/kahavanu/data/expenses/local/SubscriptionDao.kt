package com.kahavanu.data.expenses.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isDeleted = 0")
    fun observeActiveSubscriptions(userId: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isDeleted = 0")
    suspend fun getActiveSubscriptions(userId: String): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE localId = :localId")
    suspend fun getById(localId: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE clientId = :clientId")
    suspend fun getByClientId(clientId: String): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE remoteId = :remoteId")
    suspend fun getByRemoteId(remoteId: String): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: SubscriptionEntity): Long

    @Query("UPDATE subscriptions SET isDeleted = 1, updatedAtEpochMillis = :updatedAt WHERE clientId = :id")
    suspend fun markDeleted(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE subscriptions SET isSynced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isDeleted = 0 AND isPaused = 0 AND scheduledDateEpochMillis <= :now")
    suspend fun getDueSubscriptions(userId: String, now: Long): List<SubscriptionEntity>
}
