package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch

class CategoryItemsActivity : BaseActivity() {

    private lateinit var itemDao: ItemDao
    private lateinit var adapter: ItemAdapter

    private var currentCategory: String = ""
    private var allItems: List<Item> = emptyList()
    private var filterCategories: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_items)

        val category = intent.getStringExtra("category") ?: "Category"
        currentCategory = category
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCategory)

        tvTitle.text = category

        recyclerView.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, if (resources.configuration.screenWidthDp < 380) 1 else 2)

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
                            itemDao.deleteTracked(item)
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
                intent.putExtra("expiryDate", item.expiryDate)
                intent.putExtra("lowStockThreshold", item.lowStockThreshold)

                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        findViewById<View>(R.id.inventoryBack).setOnClickListener { finish() }
        findViewById<View>(R.id.inventoryAdd).setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
        findViewById<EditText>(R.id.inventorySearch).doAfterTextChanged { applyFilters() }
        setupSpinner(R.id.inventoryStock, listOf("All stock", "Low stock", "Out of stock", "In stock"))
        setupSpinner(R.id.inventorySort, listOf("Newest", "Name A–Z", "Quantity: low first", "Value: high first"))
        findViewById<Spinner>(R.id.inventoryStock).setSelection(intent.getIntExtra("stockFilter", 0).coerceIn(0, 3))
        loadCategoryItems(category)
    }

    override fun onResume() {
        super.onResume()
        loadCategoryItems(currentCategory)
    }

    private fun setupSpinner(id: Int, labels: List<String>) {
        findViewById<Spinner>(id).apply {
            adapter = ArrayAdapter(this@CategoryItemsActivity, android.R.layout.simple_spinner_dropdown_item, labels)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, rowId: Long) { applyFilters() }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun loadCategoryItems(category: String) {
        lifecycleScope.launch {
            findViewById<View>(R.id.inventoryProgress).visibility = View.VISIBLE
            try {
                allItems = itemDao.getAllItems()
                val categories = listOf("All", "Grocery", "Electronics", "Personal Care", "Stationery")
                    .plus(allItems.map { it.category }).distinct()
                if (filterCategories != categories) {
                    filterCategories = categories
                    setupSpinner(R.id.inventoryCategory, categories)
                    findViewById<Spinner>(R.id.inventoryCategory).setSelection(
                        categories.indexOf(category).coerceAtLeast(0))
                }
                applyFilters()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                findViewById<TextView>(R.id.inventoryEmpty).apply {
                    text = "Could not load inventory. Go back and try again."
                    visibility = View.VISIBLE
                }
            } finally {
                findViewById<View>(R.id.inventoryProgress).visibility = View.GONE
            }
        }
    }

    private fun applyFilters() {
        if (!::adapter.isInitialized) return
        val query = findViewById<EditText>(R.id.inventorySearch).text.toString().trim()
        val category = findViewById<Spinner>(R.id.inventoryCategory).selectedItem?.toString() ?: "All"
        val stock = findViewById<Spinner>(R.id.inventoryStock).selectedItemPosition
        val filtered = allItems.filter {
            (category == "All" || it.category == category) &&
                (it.name.contains(query, true) || it.category.contains(query, true)) &&
                when (stock) {
                    1 -> InventoryRules.status(it.quantity, it.lowStockThreshold) == StockStatus.LOW_STOCK
                    2 -> it.quantity == 0
                    3 -> InventoryRules.status(it.quantity, it.lowStockThreshold) == StockStatus.IN_STOCK
                    else -> true
                }
        }
        val sorted = when (findViewById<Spinner>(R.id.inventorySort).selectedItemPosition) {
            1 -> filtered.sortedBy { it.name.lowercase(java.util.Locale.ROOT) }
            2 -> filtered.sortedBy { it.quantity }
            3 -> filtered.sortedByDescending { it.price * it.quantity }
            else -> filtered.sortedByDescending { it.id }
        }
        adapter.updateData(sorted)
        findViewById<TextView>(R.id.inventoryCount).text =
            "${sorted.size} items · ₹${java.text.DecimalFormat("#,##0.00").format(sorted.sumOf { it.price * it.quantity })} total"
        findViewById<TextView>(R.id.inventoryEmpty).apply {
            setText(R.string.inventory_empty)
            visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
        }
        findViewById<View>(R.id.recyclerCategory).visibility = if (sorted.isEmpty()) View.GONE else View.VISIBLE
    }
}
