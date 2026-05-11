package com.example.smartinventarymanagement

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart

    private lateinit var db: AppDatabase

    private lateinit var itemDao: ItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_analytics)

        pieChart = findViewById(R.id.pieChart)

        val db = DatabaseProvider.getDatabase(this)

        itemDao = db.itemDao()

        loadChartData()
    }

    private fun loadChartData() {

        lifecycleScope.launch {

            val items = itemDao.getAllItems()

            val groceryTotal = items
                .filter { it.category == "Grocery" }
                .sumOf { it.quantity }

            val electronicsTotal = items
                .filter { it.category == "Electronics" }
                .sumOf { it.quantity }

            val personalTotal = items
                .filter { it.category == "Personal Care" }
                .sumOf { it.quantity }

            val stationeryTotal = items
                .filter { it.category == "Stationery" }
                .sumOf { it.quantity }

            val entries = ArrayList<PieEntry>()

            if (groceryTotal > 0) {
                entries.add(PieEntry(groceryTotal.toFloat(), getString(R.string.grocery)))
            }

            if (electronicsTotal > 0) {
                entries.add(PieEntry(electronicsTotal.toFloat(), getString(R.string.electronics)))
            }

            if (personalTotal > 0) {
                entries.add(PieEntry(personalTotal.toFloat(), getString(R.string.personal_care)))
            }

            if (stationeryTotal > 0) {
                entries.add(PieEntry(stationeryTotal.toFloat(), getString(R.string.stationery)))
            }

            val dataSet = PieDataSet(
                entries,
                getString(R.string.inventory_categories)
            )

            dataSet.colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63")
            )

            dataSet.valueTextColor = Color.WHITE
            dataSet.valueTextSize = 14f

            val data = PieData(dataSet)

            pieChart.data = data

            pieChart.description.isEnabled = false

            pieChart.animateY(1500)

            pieChart.setEntryLabelColor(Color.WHITE)

            pieChart.setEntryLabelTextSize(13f)

            val currentNightMode =
                resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK

            if (currentNightMode ==
                Configuration.UI_MODE_NIGHT_YES
            ) {

                pieChart.setHoleColor(Color.parseColor("#121212"))

                pieChart.setCenterTextColor(Color.WHITE)

                pieChart.legend.textColor = Color.WHITE

            } else {

                pieChart.setHoleColor(Color.WHITE)

                pieChart.setCenterTextColor(Color.BLACK)

                pieChart.legend.textColor = Color.BLACK
            }

            pieChart.centerText = getString(R.string.inventory)

            pieChart.setCenterTextSize(18f)

            pieChart.invalidate()
        }
    }
}