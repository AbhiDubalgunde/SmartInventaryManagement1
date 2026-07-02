package com.example.smartinventarymanagement

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Item::class,
        AppNotification::class
    ],
    version = 6
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun notificationDao(): NotificationDao
}