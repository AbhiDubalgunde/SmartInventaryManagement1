package com.example.smartinventarymanagement

import androidx.room.*

@Dao
interface NotificationDao {

    @Insert
    suspend fun insert(notification: AppNotification)

    @Query(
        "SELECT * FROM notifications ORDER BY timestamp DESC"
    )
    suspend fun getAllNotifications():
            List<AppNotification>

    @Query(
        "SELECT COUNT(*) FROM notifications WHERE isRead = 0"
    )
    suspend fun getUnreadCount(): Int

    @Query(
        "DELETE FROM notifications"
    )
    suspend fun clearAll()

    @Update
    suspend fun update(notification: AppNotification)
}