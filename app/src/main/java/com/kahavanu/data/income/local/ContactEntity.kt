package com.kahavanu.data.income.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String?,
    val userId: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)
