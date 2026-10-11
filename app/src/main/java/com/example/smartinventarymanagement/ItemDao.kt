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

    @Insert
    suspend fun recordActivity(entry: InventoryActivityEntry)

    @Query("SELECT * FROM inventory_activity ORDER BY timestamp DESC, id DESC LIMIT 5")
    suspend fun recentActivity(): List<InventoryActivityEntry>

    @Transaction
    suspend fun insertTracked(item: Item): Long {
        val id = insert(item)
        recordActivity(InventoryActivityEntry(itemName = item.name, action = "added",
            quantity = item.quantity, timestamp = System.currentTimeMillis()))
        return id
    }

    @Transaction
    suspend fun updateTracked(item: Item) {
        update(item)
        recordActivity(InventoryActivityEntry(itemName = item.name, action = "updated",
            quantity = item.quantity, timestamp = System.currentTimeMillis()))
    }

    @Transaction
    suspend fun deleteTracked(item: Item) {
        delete(item)
        recordActivity(InventoryActivityEntry(itemName = item.name, action = "deleted",
            quantity = item.quantity, timestamp = System.currentTimeMillis()))
    }
}
