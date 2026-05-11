package com.example.smartinventarymanagement

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var itemDao: ItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_notifications)

        val container =
            findViewById<LinearLayout>(R.id.notificationContainer)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "inventary_db"
        ).build()

        itemDao = db.itemDao()

        lifecycleScope.launch {

            val items = itemDao.getAllItems()

            val lowStockItems =
                items.filter { it.quantity < 5 }

            if (lowStockItems.isEmpty()) {

                val emptyText = TextView(this@NotificationsActivity)

                emptyText.text =
                    getString(R.string.no_low_stock_alerts)

                emptyText.textSize = 18f

                emptyText.setPadding(
                    20,
                    60,
                    20,
                    20
                )

                container.addView(emptyText)

            } else {

                lowStockItems.forEach { item ->

                    val card = layoutInflater.inflate(
                        R.layout.notification_item,
                        container,
                        false
                    )

                    val tvTitle =
                        card.findViewById<TextView>(R.id.tvNotificationTitle)

                    val tvDesc =
                        card.findViewById<TextView>(R.id.tvNotificationDesc)

                    tvTitle.text =
                        getString(R.string.low_stock_title, item.name)

                    tvDesc.text =
                        getString(R.string.low_stock_desc, item.quantity)

                    container.addView(card)
                }
            }
        }
    }
}