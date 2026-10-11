package com.example.smartinventarymanagement

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class InventoryMigrationTest {
    @Test fun migrationPreservesAccountsAndItemsAndSupportsResetAndHistory() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-test-${UUID.randomUUID()}.db"
        var room: AppDatabase? = null
        try {
            val helper = FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
                    .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, quantity INTEGER NOT NULL, price REAL NOT NULL, category TEXT NOT NULL, isLowStock INTEGER NOT NULL, imageUri TEXT, expiryDate TEXT)")
                            db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fullName TEXT NOT NULL, phoneNumber TEXT NOT NULL, password TEXT NOT NULL, createdDate INTEGER NOT NULL)")
                            db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, type TEXT NOT NULL, itemId INTEGER NOT NULL, timestamp INTEGER NOT NULL, isRead INTEGER NOT NULL)")
                            db.execSQL("INSERT INTO items VALUES (42, 'Rice', 3, 12.5, 'Grocery', 1, NULL, '01/01/2028')")
                            db.execSQL("INSERT INTO users VALUES (7, 'Test User', '+15555550101', 'Oldpass123', 1)")
                            db.execSQL("INSERT INTO users VALUES (8, 'Other User', '+15555550102', 'Otherpass123', 1)")
                        }
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    }).build())
            helper.writableDatabase
            helper.close()
            val db = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(DatabaseProvider.MIGRATION_7_8).build()
            room = db
            val original = db.itemDao().getAllItems().single()
            assertEquals(42, original.id)
            assertEquals(5, original.lowStockThreshold)
            assertEquals("01/01/2028", original.expiryDate)
            assertNotNull(db.userDao().login("+15555550101", "Oldpass123"))
            assertEquals(0, db.userDao().resetPassword(7, "+15555550102", "Newpass123"))
            assertEquals(1, db.userDao().resetPassword(7, "+15555550101", "Newpass123"))
            assertNull(db.userDao().login("+15555550101", "Oldpass123"))
            assertNotNull(db.userDao().login("+15555550101", "Newpass123"))
            assertNotNull(db.userDao().login("+15555550102", "Otherpass123"))
            db.itemDao().updateTracked(original.copy(quantity = 10, lowStockThreshold = 15))
            assertEquals(15, db.itemDao().getAllItems().single().lowStockThreshold)
            assertEquals("updated", db.itemDao().recentActivity().single().action)
            db.itemDao().deleteTracked(original)
            assertTrue(db.itemDao().getAllItems().isEmpty())
            assertEquals("deleted", db.itemDao().recentActivity().first().action)
        } finally {
            room?.close()
            context.deleteDatabase(name)
        }
    }
}
