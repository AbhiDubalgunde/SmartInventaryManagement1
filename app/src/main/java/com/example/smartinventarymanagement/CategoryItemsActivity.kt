package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch

class CategoryItemsActivity : AppCompatActivity() {

    private lateinit var itemDao: ItemDao
    private lateinit var adapter: ItemAdapter

    private var currentCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_items)

        val category = intent.getStringExtra("category") ?: "Category"
        currentCategory = category
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCategory)

        tvTitle.text = category

        recyclerView.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 2)

        val db = DatabaseProvider.getDatabase(this)

        itemDao = db.itemDao()

        adapter = ItemAdapter(
            emptyList(),

            onDeleteClick = { item ->

                AlertDialog.Builder(this@CategoryItemsActivity)
                    .setTitle(getString(R.string.delete_item))
                    .setMessage(getString(R.string.delete_item_confirm, item.name))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        lifecycleScope.launch {
                            itemDao.delete(item)
                            loadCategoryItems(currentCategory)
                        }
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            },

            onItemClick = { item ->

                val intent = Intent(this@CategoryItemsActivity, AddItemActivity::class.java)

                intent.putExtra("id", item.id)
                intent.putExtra("name", item.name)
                intent.putExtra("qty", item.quantity)
                intent.putExtra("price", item.price)
                intent.putExtra("category", item.category)
                intent.putExtra("imageUri", item.imageUri)

                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        loadCategoryItems(category)
    }

    override fun onResume() {
        super.onResume()
        loadCategoryItems(currentCategory)
    }

    private fun loadCategoryItems(category: String) {
        lifecycleScope.launch {

            val items = itemDao.getItemsByCategory(category)

            adapter.updateData(items)
        }
    }
}