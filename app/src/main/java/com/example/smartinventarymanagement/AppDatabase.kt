package com.example.smartinventarymanagement

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Item::class,
        AppNotification::class,
        User::class,
        InventoryActivityEntry::class
    ],
    version = 8
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun notificationDao(): NotificationDao

    abstract fun userDao(): UserDao
}
