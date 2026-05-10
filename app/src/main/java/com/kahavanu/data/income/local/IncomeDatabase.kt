package com.kahavanu.data.income.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IncomeLogEntity::class, IncomeSourceEntity::class, UserSettingsEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class IncomeDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
