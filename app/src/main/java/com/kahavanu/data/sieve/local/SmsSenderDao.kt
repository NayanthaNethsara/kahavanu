package com.kahavanu.data.sieve.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsSenderDao {
    @Query("SELECT * FROM sms_senders WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAtEpochMillis DESC")
    fun observeSenders(userId: String): Flow<List<SmsSenderEntity>>

    @Query("SELECT * FROM sms_senders WHERE userId = :userId AND isSynced = 0 ORDER BY createdAtEpochMillis ASC")
    suspend fun getUnsynced(userId: String): List<SmsSenderEntity>

    @Query("SELECT * FROM sms_senders WHERE userId = :userId")
    suspend fun getSendersForUser(userId: String): List<SmsSenderEntity>

    @Query("SELECT * FROM sms_senders WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): SmsSenderEntity?

    @Query("SELECT * FROM sms_senders WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): SmsSenderEntity?

    @Query("SELECT * FROM sms_senders WHERE userId = :userId AND UPPER(senderName) = UPPER(:senderName) LIMIT 1")
    suspend fun getByName(userId: String, senderName: String): SmsSenderEntity?

    @Insert
    suspend fun insert(entity: SmsSenderEntity): Long

    @Upsert
    suspend fun upsert(entity: SmsSenderEntity): Long

    @Query("UPDATE sms_senders SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)

    @Query("DELETE FROM sms_senders WHERE localId IN (:localIds)")
    suspend fun deleteByLocalIds(localIds: List<Long>)

    @Query("DELETE FROM sms_senders WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: String)
}
