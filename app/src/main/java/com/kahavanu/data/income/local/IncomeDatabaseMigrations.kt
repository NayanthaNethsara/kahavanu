package com.kahavanu.data.income.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object IncomeDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS income_sources (
                    localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    typesCsv TEXT NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL,
                    remoteId TEXT,
                    isSynced INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_income_sources_userId ON income_sources(userId)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_settings (
                    userId TEXT PRIMARY KEY NOT NULL,
                    primaryCurrency TEXT NOT NULL,
                    secondaryCurrency TEXT NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
