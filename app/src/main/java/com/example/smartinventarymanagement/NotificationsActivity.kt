package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationsActivity : BaseActivity() {

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
                    "🎉 No low stock alerts"

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

                    val imgNotification =
                        card.findViewById<ImageView>(R.id.imgNotification)

                    val btnAction =
                        card.findViewById<Button>(R.id.btnAction)

                    if (item.quantity <= 2) {

                        tvTitle.text =
                            "Critical stock alert"

                        tvDesc.text =
                            "${item.name} almost finished"

                        imgNotification.setImageResource(R.drawable.alerts)

                        btnAction.text = "Urgent"

                    } else {

                        tvTitle.text =
                            "${item.name} running low"

                        tvDesc.text =
                            "Only ${item.quantity} items remaining"

                        imgNotification.setImageResource(R.drawable.alerts)

                        btnAction.text = "Restock"
                    }

                    // CLICK NOTIFICATION
                    card.setOnClickListener {

                        val intent =
                            Intent(
                                this@NotificationsActivity,
                                AddItemActivity::class.java
                            )

                        intent.putExtra("id", item.id)

                        intent.putExtra("name", item.name)

                        intent.putExtra("qty", item.quantity)

                        intent.putExtra("price", item.price)

                        intent.putExtra("category", item.category)

                        intent.putExtra("imageUri", item.imageUri)

                        intent.putExtra("expiryDate", item.expiryDate)

                        startActivity(intent)
                    }

                    container.addView(card)
                }
            }

            // EXPIRY ALERTS

            items.forEach { item ->

                if (!item.expiryDate.isNullOrEmpty()) {

                    val daysRemaining =
                        getDaysRemaining(item.expiryDate)

                    // SHOW ONLY IMPORTANT ALERTS

                    if (daysRemaining <= 3) {

                        val card = layoutInflater.inflate(
                            R.layout.notification_item,
                            container,
                            false
                        )

                        val tvTitle =
                            card.findViewById<TextView>(
                                R.id.tvNotificationTitle
                            )

                        val tvDesc =
                            card.findViewById<TextView>(
                                R.id.tvNotificationDesc
                            )

                        val imgNotification =
                            card.findViewById<ImageView>(
                                R.id.imgNotification
                            )

                        val btnAction =
                            card.findViewById<Button>(
                                R.id.btnAction
                            )

                        when {

                            // EXPIRED

                            daysRemaining < 0 -> {

                                tvTitle.text =
                                    "${item.name} expired"

                                tvDesc.text =
                                    "Remove or replace immediately"

                                btnAction.text =
                                    "Remove"

                                imgNotification.setImageResource(
                                    R.drawable.alerts
                                )
                            }

                            // TODAY

                            daysRemaining == 0L -> {

                                tvTitle.text =
                                    "${item.name} expires today"

                                tvDesc.text =
                                    "Immediate attention needed"

                                btnAction.text =
                                    "Update"

                                imgNotification.setImageResource(
                                    R.drawable.alerts
                                )
                            }

                            // TOMORROW

                            daysRemaining == 1L -> {

                                tvTitle.text =
                                    "${item.name} expires tomorrow"

                                tvDesc.text =
                                    "Plan restocking soon"

                                btnAction.text =
                                    "Check"

                                imgNotification.setImageResource(
                                    R.drawable.alerts
                                )
                            }

                            // FEW DAYS LEFT

                            else -> {

                                tvTitle.text =
                                    "${item.name} expiring soon"

                                tvDesc.text =
                                    "Expires in $daysRemaining days"

                                btnAction.text =
                                    "View"

                                imgNotification.setImageResource(
                                    R.drawable.alerts
                                )
                            }
                        }

                        // CLICK ACTION

                        card.setOnClickListener {

                            val intent =
                                Intent(
                                    this@NotificationsActivity,
                                    AddItemActivity::class.java
                                )

                            intent.putExtra("id", item.id)

                            intent.putExtra("name", item.name)

                            intent.putExtra("qty", item.quantity)

                            intent.putExtra("price", item.price)

                            intent.putExtra("category", item.category)

                            intent.putExtra("imageUri", item.imageUri)

                            intent.putExtra("expiryDate", item.expiryDate)

                            startActivity(intent)
                        }

                        container.addView(card)
                    }
                }
            }
        }
    }

    private fun getDaysRemaining(expiryDate: String): Long {

        return try {

            val format = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            val expiry =
                format.parse(expiryDate)

            val today =
                Calendar.getInstance().time

            val difference =
                expiry.time - today.time

            TimeUnit.MILLISECONDS.toDays(difference)

        } catch (e: Exception) {

            -999
        }
    }
}