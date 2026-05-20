package com.kahavanu.data.sieve.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNew(entity: SmsSuggestionEntity): Long

    @Update
    suspend fun update(entity: SmsSuggestionEntity)

    @Query("SELECT * FROM sms_suggestions WHERE userId = :userId AND status = 'PENDING' ORDER BY smsReceivedAtEpochMillis DESC")
    fun observePending(userId: String): Flow<List<SmsSuggestionEntity>>

    @Query("SELECT * FROM sms_suggestions WHERE userId = :userId AND status = 'PENDING' AND kind IN (:kinds) ORDER BY smsReceivedAtEpochMillis DESC")
    fun observePendingByKinds(userId: String, kinds: List<String>): Flow<List<SmsSuggestionEntity>>

    @Query("SELECT * FROM sms_suggestions WHERE localId = :id")
    suspend fun getById(id: Long): SmsSuggestionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM sms_suggestions WHERE smsBodyHash = :hash)")
    suspend fun existsByHash(hash: String): Boolean

    @Query("UPDATE sms_suggestions SET status = :status, updatedAtEpochMillis = :updatedAt WHERE localId = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long)
}
