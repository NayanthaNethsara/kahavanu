package com.kahavanu.data.income.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeSourceDao {
    @Query(
        "SELECT * FROM income_sources WHERE userId = :userId AND isDeleted = 0 ORDER BY name ASC"
    )
    fun observeSources(userId: String): Flow<List<IncomeSourceEntity>>

    @Query("SELECT COUNT(*) FROM income_sources WHERE userId = :userId AND isDeleted = 0")
    suspend fun countActiveSources(userId: String): Int

    @Query("SELECT * FROM income_sources WHERE userId = :userId AND isDeleted = 0")
    suspend fun getActiveSources(userId: String): List<IncomeSourceEntity>

    @Query("SELECT * FROM income_sources WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsynced(userId: String): List<IncomeSourceEntity>

    @Query("SELECT * FROM income_sources WHERE localId = :localId LIMIT 1")
    suspend fun getById(localId: Long): IncomeSourceEntity?

    @Query("SELECT * FROM income_sources WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): IncomeSourceEntity?

    @Query("SELECT * FROM income_sources WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): IncomeSourceEntity?

    @Upsert
    suspend fun upsert(entity: IncomeSourceEntity): Long

    @Query("UPDATE income_sources SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String?)

    @Query(
        "SELECT remoteId FROM income_sources WHERE userId = :userId AND remoteId IS NOT NULL AND isSynced = 1"
    )
    suspend fun getSyncedRemoteIds(userId: String): List<String>

    @Query(
        "UPDATE income_sources SET isDeleted = 1, isSynced = 1 WHERE userId = :userId AND remoteId IN (:remoteIds)"
    )
    suspend fun markDeletedByRemoteIds(userId: String, remoteIds: List<String>)

    @Query("DELETE FROM income_sources WHERE localId IN (:localIds)")
    suspend fun deleteByLocalIds(localIds: List<Long>)

    @Query(
        "UPDATE income_sources SET isDeleted = 1, isSynced = 0, updatedAtEpochMillis = :updatedAtEpochMillis WHERE localId = :localId"
    )
    suspend fun markDeleted(localId: Long, updatedAtEpochMillis: Long)
}
