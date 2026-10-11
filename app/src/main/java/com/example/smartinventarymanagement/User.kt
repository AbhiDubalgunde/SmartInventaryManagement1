package com.example.smartinventarymanagement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val createdDate: Long = System.currentTimeMillis()
)