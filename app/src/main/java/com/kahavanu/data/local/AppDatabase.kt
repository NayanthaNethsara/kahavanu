package com.kahavanu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kahavanu.data.expenses.local.ExpenseLogEntity
import com.kahavanu.data.expenses.local.ExpenseLogDao
import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.data.income.local.ScheduledIncomeDao
import com.kahavanu.data.settings.local.UserSettingsDao
import com.kahavanu.data.settings.local.UserSettingsEntity

@Database(
    entities = [
        IncomeLogEntity::class, 
        IncomeSourceEntity::class, 
        UserSettingsEntity::class,
        ScheduledIncomeEntity::class,
        ExpenseLogEntity::class
    ],
    version = 14,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun scheduledIncomeDao(): ScheduledIncomeDao
    abstract fun expenseLogDao(): ExpenseLogDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
