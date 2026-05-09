package com.kahavanu.data.income.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IncomeLogEntity::class, IncomeSourceEntity::class, UserSettingsEntity::class, ContactEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class IncomeDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun contactDao(): ContactDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
