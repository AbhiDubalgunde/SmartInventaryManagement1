package com.example.smartinventarymanagement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_activity")
data class InventoryActivityEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemName: String,
    val action: String,
    val quantity: Int,
    val timestamp: Long
)
