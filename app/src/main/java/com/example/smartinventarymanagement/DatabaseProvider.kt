package com.example.smartinventarymanagement

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    private var INSTANCE: AppDatabase? = null

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE items ADD COLUMN lowStockThreshold INTEGER NOT NULL DEFAULT 5")
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS inventory_activity (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    itemName TEXT NOT NULL,
                    action TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }

    private val MIGRATION_2_3 =
        object : Migration(2, 3) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    "ALTER TABLE items ADD COLUMN expiryDate TEXT"
                )
            }
        }

    private val MIGRATION_3_4 =
        object : Migration(3, 4) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        type TEXT NOT NULL,
                        itemId INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

    private val MIGRATION_5_6 =
        object : Migration(5, 6) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS notifications_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    type TEXT NOT NULL,
                    itemId INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    isRead INTEGER NOT NULL
                )
                """.trimIndent()
                )

                database.execSQL(
                    """
                INSERT INTO notifications_new
                (id, title, description, type, itemId, timestamp, isRead)

                SELECT
                id,
                title,
                message,
                type,
                itemId,
                0,
                0

                FROM notifications
                """.trimIndent()
                )

                database.execSQL(
                    "DROP TABLE notifications"
                )

                database.execSQL(
                    "ALTER TABLE notifications_new RENAME TO notifications"
                )
            }
        }

    fun getDatabase(context: Context): AppDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventary_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_5_6,
                        MIGRATION_7_8
                    )
                    .fallbackToDestructiveMigration()
                    .build()

            INSTANCE = instance

            instance
        }
    }
}
