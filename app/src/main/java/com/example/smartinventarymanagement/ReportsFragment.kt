package com.example.smartinventarymanagement

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.content.Intent
import android.view.GestureDetector
import android.view.MotionEvent
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {

    private lateinit var pieChart: PieChart

    private lateinit var barChart: BarChart
    private lateinit var db: AppDatabase

    private lateinit var itemDao: ItemDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_reports,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = view.findViewById(R.id.pieChart)

        barChart = view.findViewById(R.id.barChart)

        val tvTotalItems = view.findViewById<TextView>(R.id.tvTotalItems)
        val tvLowStock = view.findViewById<TextView>(R.id.tvLowStock)
        val tvInventoryValue = view.findViewById<TextView>(R.id.tvInventoryValue)
        val tvTopCategory = view.findViewById<TextView>(R.id.tvTopCategory)
        val tvHealth = view.findViewById<TextView>(R.id.tvHealth)
        val tvAlerts = view.findViewById<TextView>(R.id.tvAlerts)
        val tvUpdated = view.findViewById<TextView>(R.id.tvUpdated)
        tvUpdated.text = getString(R.string.updated_just_now)

        db = DatabaseProvider.getDatabase(requireContext())

        itemDao = db.itemDao()

        lifecycleScope.launch {

            val items = itemDao.getAllItems()

            val totalItems = items.sumOf { it.quantity }

            val totalValue = items.sumOf {
                it.price * it.quantity
            }

            val lowStockItems = items.filter {
                it.quantity < 5
            }

            animateText(
                tvTotalItems,
                0,
                totalItems
            )

            animateText(
                tvLowStock,
                0,
                lowStockItems.size
            )

            tvInventoryValue.text = "₹$totalValue"

            // CATEGORY TOTALS

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

            // PIE CHART

            val entries = ArrayList<PieEntry>()

            if (groceryTotal > 0) {
                entries.add(PieEntry(groceryTotal.toFloat(), "Grocery"))
            }

            if (electronicsTotal > 0) {
                entries.add(PieEntry(electronicsTotal.toFloat(), "Electronics"))
            }

            if (personalTotal > 0) {
                entries.add(PieEntry(personalTotal.toFloat(), "Personal Care"))
            }

            if (stationeryTotal > 0) {
                entries.add(PieEntry(stationeryTotal.toFloat(), "Stationery"))
            }

            val dataSet = PieDataSet(
                entries,
                getString(R.string.inventory_items)
            )

            dataSet.colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63")
            )

            dataSet.valueTextColor = Color.WHITE

            dataSet.valueTextSize = 14f

            dataSet.selectionShift = 0f // Disable shift to prevent touch issues

            val data = PieData(dataSet)

            pieChart.data = data

            pieChart.description.isEnabled = false

            pieChart.centerText = getString(R.string.inventory_items)

            pieChart.setCenterTextSize(18f)

            pieChart.setEntryLabelColor(Color.WHITE)

            val nightMode =
                resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK

            if (nightMode ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            ) {

                // DARK MODE

                pieChart.setCenterTextColor(Color.WHITE)

                pieChart.setHoleColor(Color.parseColor("#121212"))

                pieChart.legend.textColor = Color.WHITE

            } else {

                // LIGHT MODE

                pieChart.setCenterTextColor(Color.BLACK)

                pieChart.setHoleColor(Color.WHITE)

                pieChart.legend.textColor = Color.BLACK
            }

            pieChart.animateY(1500)

            pieChart.isRotationEnabled = false

            val pieGestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val h = pieChart.getHighlightByTouchPoint(e.x, e.y)
                    if (h != null) {
                        val entry = pieChart.data.getEntryForHighlight(h) as? PieEntry
                        entry?.label?.let { openCategory(it) }
                    }
                    return true
                }
            })
            
            pieChart.setOnTouchListener { _, event ->
                event?.let { pieGestureDetector.onTouchEvent(it) }
                false
            }

            pieChart.invalidate()


            //Bar Chart
            // BAR CHART

            val barEntries = ArrayList<BarEntry>()

            barEntries.add(
                BarEntry(0f, groceryTotal.toFloat())
            )

            barEntries.add(
                BarEntry(1f, electronicsTotal.toFloat())
            )

            barEntries.add(
                BarEntry(2f, personalTotal.toFloat())
            )

            barEntries.add(
                BarEntry(3f, stationeryTotal.toFloat())
            )

            val barDataSet = BarDataSet(
                barEntries,
                getString(R.string.category_stock)
            )

            barDataSet.colors = listOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#E91E63")
            )

            barDataSet.valueTextColor = Color.WHITE

            barDataSet.valueTextSize = 13f

            val barData = BarData(barDataSet)

            barChart.data = barData

            val labels = listOf(
                getString(R.string.grocery),
                getString(R.string.electronics),
                getString(R.string.personal_care),
                getString(R.string.stationery)
            )

            barChart.xAxis.valueFormatter =
                IndexAxisValueFormatter(labels)

            barChart.xAxis.granularity = 1f

            barChart.description.isEnabled = false

            barChart.isDoubleTapToZoomEnabled = false

            val barGestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val h = barChart.getHighlightByTouchPoint(e.x, e.y)
                    if (h != null) {
                        val index = h.x.toInt()
                        val labels = listOf("Grocery", "Electronics", "Personal Care", "Stationery")
                        if (index in labels.indices) {
                            openCategory(labels[index])
                        }
                    }
                    return true
                }
            })

            barChart.setOnTouchListener { _, event ->
                event?.let { barGestureDetector.onTouchEvent(it) }
                false
            }

            barChart.animateY(1500)

            barChart.invalidate()


            // TOP CATEGORY

            val categoryMap = mapOf(
                "Grocery" to groceryTotal,
                "Electronics" to electronicsTotal,
                "Personal Care" to personalTotal,
                "Stationery" to stationeryTotal
            )

            val topCategory = categoryMap.maxByOrNull {
                it.value
            }

            tvTopCategory.text =
                getString(R.string.top_category_insight, topCategory?.key)

            // HEALTH STATUS

            tvHealth.text = when {

                lowStockItems.isEmpty() ->
                    getString(R.string.health_excellent)

                lowStockItems.size < 5 ->
                    getString(R.string.health_moderate)

                else ->
                    getString(R.string.health_critical)
            }

            // LOW STOCK ALERTS

            if (lowStockItems.isEmpty()) {

                tvAlerts.text = getString(R.string.no_low_stock_alerts)

            } else {

                val alertText = StringBuilder()

                lowStockItems.forEach {

                    alertText.append(
                        "• ${it.name} → Qty: ${it.quantity}\n"
                    )
                }

                tvAlerts.text = alertText.toString()
            }
        }
    }

    private fun openCategory(category: String) {
        val intent = Intent(requireContext(), CategoryItemsActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun animateText(
        textView: TextView,
        start: Int,
        end: Int
    ) {

        val animator = ValueAnimator.ofInt(
            start,
            end
        )

        animator.duration = 1200

        animator.addUpdateListener {

            textView.text =
                it.animatedValue.toString()
        }

        animator.start()
    }
}