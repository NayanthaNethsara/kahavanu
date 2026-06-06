package com.kahavanu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kahavanu.data.expenses.local.ExpenseLogEntity
import com.kahavanu.data.expenses.local.ExpenseLogDao
import com.kahavanu.data.expenses.local.SubscriptionEntity
import com.kahavanu.data.expenses.local.SubscriptionDao
import com.kahavanu.data.goals.local.GoalAdjustmentLogDao
import com.kahavanu.data.goals.local.GoalAdjustmentLogEntity
import com.kahavanu.data.goals.local.GoalLogDao
import com.kahavanu.data.goals.local.GoalLogEntity
import com.kahavanu.data.income.local.IncomeLogEntity
import com.kahavanu.data.income.local.IncomeLogDao
import com.kahavanu.data.income.local.IncomeSourceEntity
import com.kahavanu.data.income.local.IncomeSourceDao
import com.kahavanu.data.income.local.ScheduledIncomeEntity
import com.kahavanu.data.income.local.ScheduledIncomeDao
import com.kahavanu.data.notifications.local.NotificationDao
import com.kahavanu.data.notifications.local.NotificationEntity
import com.kahavanu.data.settings.local.UserSettingsDao
import com.kahavanu.data.settings.local.UserSettingsEntity
import com.kahavanu.data.sieve.local.SmsSenderDao
import com.kahavanu.data.sieve.local.SmsSenderEntity
import com.kahavanu.data.sieve.local.SmsSuggestionDao
import com.kahavanu.data.sieve.local.SmsSuggestionEntity

@Database(
    entities = [
        IncomeLogEntity::class,
        IncomeSourceEntity::class,
        UserSettingsEntity::class,
        ScheduledIncomeEntity::class,
        ExpenseLogEntity::class,
        GoalLogEntity::class,
        GoalAdjustmentLogEntity::class,
        SmsSenderEntity::class,
        SmsSuggestionEntity::class,
        SubscriptionEntity::class,
        NotificationEntity::class,
    ],
    version = 26,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incomeLogDao(): IncomeLogDao
    abstract fun incomeSourceDao(): IncomeSourceDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun scheduledIncomeDao(): ScheduledIncomeDao
    abstract fun expenseLogDao(): ExpenseLogDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun goalLogDao(): GoalLogDao
    abstract fun goalAdjustmentLogDao(): GoalAdjustmentLogDao
    abstract fun smsSenderDao(): SmsSenderDao
    abstract fun smsSuggestionDao(): SmsSuggestionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DB_NAME = "kahavanu.db"
    }
}
