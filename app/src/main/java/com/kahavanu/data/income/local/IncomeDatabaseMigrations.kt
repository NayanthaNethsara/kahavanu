package com.kahavanu.data.income.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object IncomeDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
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
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_income_sources_userId ON income_sources(userId)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
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

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS contacts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    phoneNumber TEXT,
                    userId TEXT NOT NULL,
                    lastUsedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_contacts_userId ON contacts(userId)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE income_logs ADD COLUMN contactName TEXT")
            db.execSQL("ALTER TABLE income_logs ADD COLUMN contactNumber TEXT")
            db.execSQL("DROP TABLE IF EXISTS contacts")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE income_logs ADD COLUMN sourceId INTEGER")
            db.execSQL("ALTER TABLE income_logs ADD COLUMN sourceType TEXT")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE income_logs ADD COLUMN isInvoiceSent INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE income_logs ADD COLUMN frequency TEXT")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE income_logs ADD COLUMN sourceName TEXT")
        }
    }
}
