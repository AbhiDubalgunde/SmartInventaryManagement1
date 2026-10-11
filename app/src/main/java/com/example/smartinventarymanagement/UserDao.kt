package com.example.smartinventarymanagement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): User?

    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password LIMIT 1")
    suspend fun login(phoneNumber: String, password: String): User?

    @Query("UPDATE users SET password = :password WHERE id = :userId AND phoneNumber = :phone")
    suspend fun resetPassword(userId: Int, phone: String, password: String): Int
}
