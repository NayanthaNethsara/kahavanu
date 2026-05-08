package com.kahavanu.data.income.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IncomeLogEntity::class, IncomeSourceEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class IncomeDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
