package com.kahavanu.data.income.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IncomeLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class IncomeDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
