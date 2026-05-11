package com.example.smartinventarymanagement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val quantity: Int,

    val price: Double,

    val category: String,

    val isLowStock: Boolean,

    val imageUri: String? = null
)