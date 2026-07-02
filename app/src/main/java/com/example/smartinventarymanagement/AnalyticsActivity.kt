package com.example.smartinventarymanagement

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class AnalyticsActivity : BaseActivity() {

    private lateinit var pieChart: PieChart

    private lateinit var itemDao: ItemDao

    private lateinit var tvAnalyticsTitle: TextView

    private lateinit var tvLegendElectronics: TextView
    private lateinit var tvLegendGrocery: TextView
    private lateinit var tvLegendPersonal: TextView
    private lateinit var tvLegendStationery: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_analytics)

        pieChart =
            findViewById(R.id.pieChart)

        tvAnalyticsTitle =
            findViewById(R.id.tvAnalyticsTitle)

        tvLegendElectronics =
            findViewById(R.id.tvLegendElectronics)

        tvLegendGrocery =
            findViewById(R.id.tvLegendGrocery)

        tvLegendPersonal =
            findViewById(R.id.tvLegendPersonal)

        tvLegendStationery =
            findViewById(R.id.tvLegendStationery)

        val db =
            DatabaseProvider.getDatabase(this)

        itemDao =
            db.itemDao()

        val analyticsType =
            intent.getStringExtra(
                "analytics_type"
            ) ?: "items"

        loadChartData(analyticsType)
    }

    private fun loadChartData(
        type: String
    ) {

        lifecycleScope.launch {

            val items =
                itemDao.getAllItems()

            val grocery =
                items.filter {
                    it.category == "Grocery"
                }

            val electronics =
                items.filter {
                    it.category == "Electronics"
                }

            val personal =
                items.filter {
                    it.category == "Personal Care"
                }

            val stationery =
                items.filter {
                    it.category == "Stationery"
                }

            val entries =
                ArrayList<PieEntry>()

            if (type == "items") {

                tvAnalyticsTitle.text =
                    "Inventory Distribution"

                val groceryTotal =
                    grocery.sumOf { it.quantity }

                val electronicsTotal =
                    electronics.sumOf { it.quantity }

                val personalTotal =
                    personal.sumOf { it.quantity }

                val stationeryTotal =
                    stationery.sumOf { it.quantity }

                tvLegendElectronics.text =
                    "🟦 Electronics   $electronicsTotal Items"

                tvLegendGrocery.text =
                    "🟩 Grocery   $groceryTotal Items"

                tvLegendPersonal.text =
                    "🟧 Personal Care   $personalTotal Items"

                tvLegendStationery.text =
                    "🟥 Stationery   $stationeryTotal Items"

                if (groceryTotal > 0) {

                    entries.add(
                        PieEntry(
                            groceryTotal.toFloat(),
                            "Grocery"
                        )
                    )
                }

                if (electronicsTotal > 0) {

                    entries.add(
                        PieEntry(
                            electronicsTotal.toFloat(),
                            "Electronics"
                        )
                    )
                }

                if (personalTotal > 0) {

                    entries.add(
                        PieEntry(
                            personalTotal.toFloat(),
                            "Personal Care"
                        )
                    )
                }

                if (stationeryTotal > 0) {

                    entries.add(
                        PieEntry(
                            stationeryTotal.toFloat(),
                            "Stationery"
                        )
                    )
                }

                pieChart.centerText =
                    "Items"

            } else {

                tvAnalyticsTitle.text =
                    "Value Distribution"

                val groceryValue =
                    grocery.sumOf {
                        it.price * it.quantity
                    }

                val electronicsValue =
                    electronics.sumOf {
                        it.price * it.quantity
                    }

                val personalValue =
                    personal.sumOf {
                        it.price * it.quantity
                    }

                val stationeryValue =
                    stationery.sumOf {
                        it.price * it.quantity
                    }

                tvLegendElectronics.text =
                    "🟦 Electronics   ${
                        formatValue(electronicsValue)
                    }"

                tvLegendGrocery.text =
                    "🟩 Grocery   ${
                        formatValue(groceryValue)
                    }"

                tvLegendPersonal.text =
                    "🟧 Personal Care   ${
                        formatValue(personalValue)
                    }"

                tvLegendStationery.text =
                    "🟥 Stationery   ${
                        formatValue(stationeryValue)
                    }"

                if (groceryValue > 0) {

                    entries.add(
                        PieEntry(
                            groceryValue.toFloat(),
                            "Grocery"
                        )
                    )
                }

                if (electronicsValue > 0) {

                    entries.add(
                        PieEntry(
                            electronicsValue.toFloat(),
                            "Electronics"
                        )
                    )
                }

                if (personalValue > 0) {

                    entries.add(
                        PieEntry(
                            personalValue.toFloat(),
                            "Personal Care"
                        )
                    )
                }

                if (stationeryValue > 0) {

                    entries.add(
                        PieEntry(
                            stationeryValue.toFloat(),
                            "Stationery"
                        )
                    )
                }

                pieChart.centerText =
                    "Value"
            }

            val dataSet =
                PieDataSet(entries, "")

            dataSet.sliceSpace = 3f

            dataSet.selectionShift = 8f

            dataSet.colors = listOf(

                Color.parseColor("#4CAF50"),

                Color.parseColor("#2196F3"),

                Color.parseColor("#FF9800"),

                Color.parseColor("#E91E63")
            )

            dataSet.valueTextColor =
                Color.WHITE

            dataSet.valueTextSize =
                12f

            val data =
                PieData(dataSet)

            pieChart.setUsePercentValues(true)

            data.setValueFormatter(
                PercentFormatter(pieChart)
            )

            pieChart.data =
                data

            pieChart.description.isEnabled =
                false

            pieChart.legend.isEnabled =
                false

            pieChart.animateY(1500)

            pieChart.setDrawEntryLabels(false)

            pieChart.holeRadius =
                45f

            pieChart.transparentCircleRadius =
                70f

            pieChart.setCenterTextSize(22f)

            val currentNightMode =
                resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK

            if (
                currentNightMode ==
                Configuration.UI_MODE_NIGHT_YES
            ) {

                pieChart.setHoleColor(
                    Color.parseColor("#121212")
                )

                pieChart.setCenterTextColor(
                    Color.WHITE
                )

                pieChart.legend.textColor =
                    Color.WHITE

            } else {

                pieChart.setHoleColor(
                    Color.WHITE
                )

                pieChart.setCenterTextColor(
                    Color.BLACK
                )

                pieChart.legend.textColor =
                    Color.BLACK

//                pieChart.legend.isEnabled = false
            }

            pieChart.invalidate()
        }
    }

    private fun formatValue(
        value: Double
    ): String {
        return when {

            value >= 10000000 ->

                "₹${
                    DecimalFormat("#.#")
                        .format(value / 10000000)
                }Cr"

            value >= 100000 ->

                "₹${
                    DecimalFormat("#.#")
                        .format(value / 100000)
                }L"

            value >= 1000 ->

                "₹${
                    DecimalFormat("#,###")
                        .format(value)
                }"

            else ->

                "₹${value.toInt()}"
        }
    }
}