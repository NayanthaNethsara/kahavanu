package com.kahavanu.data.goals.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalLogDao {
    @Query("SELECT * FROM goal_logs WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAtEpochMillis DESC")
    fun observeGoals(userId: String): Flow<List<GoalLogEntity>>

    @Query("SELECT * FROM goal_logs WHERE userId = :userId AND isSynced = 0 ORDER BY createdAtEpochMillis ASC")
    suspend fun getUnsynced(userId: String): List<GoalLogEntity>

    @Query("SELECT * FROM goal_logs WHERE userId = :userId")
    suspend fun getGoalsForUser(userId: String): List<GoalLogEntity>

    @Query("SELECT * FROM goal_logs WHERE userId = :userId AND isDeleted = 0 AND isCompleted = 0")
    suspend fun getActiveGoalsForUser(userId: String): List<GoalLogEntity>

    @Query("SELECT * FROM goal_logs WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): GoalLogEntity?

    @Query("SELECT * FROM goal_logs WHERE clientId = :clientId LIMIT 1")
    suspend fun getByClientId(clientId: String): GoalLogEntity?

    @Insert
    suspend fun insert(entity: GoalLogEntity): Long

    @Upsert
    suspend fun upsert(entity: GoalLogEntity): Long

    @Upsert
    suspend fun upsertAll(entities: List<GoalLogEntity>)

    @Query("UPDATE goal_logs SET remoteId = :remoteId, isSynced = 1 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: String)

    @Query("DELETE FROM goal_logs WHERE localId IN (:localIds)")
    suspend fun deleteByLocalIds(localIds: List<Long>)

    @Query("DELETE FROM goal_logs WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: String)
}
