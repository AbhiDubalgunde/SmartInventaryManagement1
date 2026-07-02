package com.example.smartinventarymanagement

import androidx.room.*

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: Item): Long

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Delete
    suspend fun delete(item: Item)

    @Update
    suspend fun update(item: Item)

    @Query("SELECT * FROM items WHERE category = :category")
    suspend fun getItemsByCategory(category: String): List<Item>
}