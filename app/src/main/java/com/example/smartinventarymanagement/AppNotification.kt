package com.example.smartinventarymanagement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotification(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val type: String,

    val itemId: Int,

    val timestamp: Long = System.currentTimeMillis(),

    val isRead: Boolean = false
)