package com.kahavanu.data.income.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kahavanu.data.settings.local.UserSettingsDao
import com.kahavanu.data.settings.local.UserSettingsEntity

@Database(
    entities = [
        IncomeLogEntity::class, 
        IncomeSourceEntity::class, 
        UserSettingsEntity::class,
        ScheduledIncomeEntity::class
    ],
    version = 11,
    exportSchema = false,
)
abstract class IncomeDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun scheduledIncomeDao(): ScheduledIncomeDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
