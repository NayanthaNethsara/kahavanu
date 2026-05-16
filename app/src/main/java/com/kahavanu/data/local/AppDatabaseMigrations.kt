package com.kahavanu.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
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

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS scheduled_income (
                    localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    type TEXT NOT NULL,
                    frequency TEXT,
                    scheduledDateEpochMillis INTEGER NOT NULL,
                    lastGeneratedEpochMillis INTEGER,
                    sourceId INTEGER,
                    sourceName TEXT,
                    isInvoiceSent INTEGER NOT NULL DEFAULT 0,
                    contactName TEXT,
                    contactNumber TEXT,
                    remoteId TEXT,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )

            try {
                db.execSQL("ALTER TABLE income_logs ADD COLUMN remoteId TEXT")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE income_logs ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE income_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE income_logs ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE income_logs SET updatedAtEpochMillis = createdAtEpochMillis WHERE updatedAtEpochMillis = 0"
                )
            } catch (_: Exception) {
            }

            try {
                db.execSQL("ALTER TABLE scheduled_income ADD COLUMN remoteId TEXT")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE scheduled_income ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE scheduled_income ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            } catch (_: Exception) {
            }
            try {
                db.execSQL("ALTER TABLE scheduled_income ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE scheduled_income SET updatedAtEpochMillis = scheduledDateEpochMillis WHERE updatedAtEpochMillis = 0"
                )
            } catch (_: Exception) {
            }
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE income_logs ADD COLUMN clientId TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                "UPDATE income_logs SET clientId = CASE " +
                    "WHEN remoteId IS NOT NULL THEN remoteId " +
                    "ELSE 'local-' || localId END " +
                    "WHERE clientId = ''"
            )

            db.execSQL("ALTER TABLE income_sources ADD COLUMN clientId TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                "UPDATE income_sources SET clientId = CASE " +
                    "WHEN remoteId IS NOT NULL THEN remoteId " +
                    "ELSE 'local-' || localId END " +
                    "WHERE clientId = ''"
            )

            db.execSQL("ALTER TABLE scheduled_income ADD COLUMN clientId TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                "UPDATE scheduled_income SET clientId = CASE " +
                    "WHEN remoteId IS NOT NULL THEN remoteId " +
                    "ELSE 'local-' || localId END " +
                    "WHERE clientId = ''"
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE scheduled_income ADD COLUMN occurrenceCount INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS expense_logs (
                    localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    spentAtEpochMillis INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL,
                    merchant TEXT,
                    category TEXT NOT NULL,
                    notes TEXT,
                    paymentMethod TEXT NOT NULL,
                    clientId TEXT NOT NULL,
                    remoteId TEXT,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expense_logs_userId ON expense_logs(userId)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expense_logs_spentAt ON expense_logs(spentAtEpochMillis)"
            )
        }
    }

    // Recreates expense_logs to match the entity exactly:
    //   - paymentMethod is nullable (TEXT, no NOT NULL)
    //   - isSynced / isDeleted have no explicit DEFAULT clause
    //   - drops the extra index_expense_logs_spentAt that was created in 12->13
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS expense_logs_new (
                    localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    spentAtEpochMillis INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL,
                    merchant TEXT,
                    category TEXT NOT NULL,
                    notes TEXT,
                    paymentMethod TEXT,
                    clientId TEXT NOT NULL,
                    remoteId TEXT,
                    isSynced INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO expense_logs_new
                    (localId, userId, title, amount, currency, spentAtEpochMillis,
                     createdAtEpochMillis, merchant, category, notes, paymentMethod,
                     clientId, remoteId, isSynced, isDeleted, updatedAtEpochMillis)
                SELECT  localId, userId, title, amount, currency, spentAtEpochMillis,
                        createdAtEpochMillis, merchant, category, notes, paymentMethod,
                        clientId, remoteId, isSynced, isDeleted, updatedAtEpochMillis
                FROM expense_logs
                """.trimIndent()
            )
            db.execSQL("DROP TABLE expense_logs")
            db.execSQL("ALTER TABLE expense_logs_new RENAME TO expense_logs")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_expense_logs_userId ON expense_logs(userId)"
            )
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_goal_logs_clientId ON goal_logs(clientId)"
            )
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS goal_logs (
                    localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    targetAmount REAL NOT NULL,
                    currentAmount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    category TEXT NOT NULL,
                    targetDateEpochMillis INTEGER,
                    isCompleted INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL,
                    clientId TEXT NOT NULL,
                    remoteId TEXT,
                    isSynced INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_goal_logs_userId ON goal_logs(userId)"
            )
        }
    }
}
