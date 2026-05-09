package com.kahavanu.data.income.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY lastUsedAt DESC")
    fun observeContacts(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND name = :name AND (phoneNumber = :phoneNumber OR :phoneNumber IS NULL) LIMIT 1")
    suspend fun getContact(userId: String, name: String, phoneNumber: String?): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: ContactEntity): Long

    @Query("UPDATE contacts SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)
}
