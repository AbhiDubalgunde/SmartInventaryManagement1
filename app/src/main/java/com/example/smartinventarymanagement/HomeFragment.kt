package com.example.smartinventarymanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar
import android.widget.GridLayout
import com.bumptech.glide.Glide

class HomeFragment : Fragment(), SearchFragment.OnSearchListener {

    private lateinit var db: AppDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var adapter: ItemAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalValue: TextView

    private lateinit var tvInsight1: TextView
    private lateinit var tvInsight2: TextView
    private lateinit var tvInsight3: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SEARCH FRAGMENT
        childFragmentManager.beginTransaction()
            .replace(R.id.searchContainer, SearchFragment())
            .commit()

        // DATABASE
        db = DatabaseProvider.getDatabase(requireContext())

        itemDao = db.itemDao()

        // RECYCLER VIEW
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )

        adapter = ItemAdapter(
            emptyList(),

            // DELETE
            onDeleteClick = { item ->

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_item))
                    .setMessage(getString(R.string.delete_item_confirm, item.name))

                    .setPositiveButton(getString(R.string.yes)) { _, _ ->

                        lifecycleScope.launch {

                            itemDao.delete(item)

                            loadItems()
                        }
                    }

                    .setNegativeButton(getString(R.string.no), null)

                    .show()
            },

            // EDIT
            onItemClick = { item ->

                val intent = Intent(
                    requireContext(),
                    AddItemActivity::class.java
                )

                intent.putExtra("id", item.id)

                intent.putExtra("name", item.name)

                intent.putExtra("qty", item.quantity)

                intent.putExtra("price", item.price)

                intent.putExtra("category", item.category)

                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter

        //ai insights
        tvInsight1 =
            view.findViewById(R.id.tvInsight1)

        tvInsight2 =
            view.findViewById(R.id.tvInsight2)

        tvInsight3 =
            view.findViewById(R.id.tvInsight3)


        // VIEWS
        val fabAdd =
            view.findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)

        val analyticsCard =
            view.findViewById<LinearLayout>(R.id.analyticsCard)

        val cardGrocery =
            view.findViewById<LinearLayout>(R.id.cardGrocery)

        val cardElectronics =
            view.findViewById<LinearLayout>(R.id.cardElectronics)

        val cardPersonal =
            view.findViewById<LinearLayout>(R.id.cardPersonal)

        val cardStationery =
            view.findViewById<LinearLayout>(R.id.cardStationery)

        val tvGreeting =
            view.findViewById<TextView>(R.id.tvGreeting)

        val tvAnimatedText =
            view.findViewById<TextView>(R.id.tvAnimatedText)

        val imgDashboardProfile =
            view.findViewById<ImageView>(R.id.imgDashboardProfile)

        val ivNotification =
            view.findViewById<ImageView>(R.id.ivNotification)

        val tvBadge =
            view.findViewById<TextView>(R.id.tvBadge)

        tvTotalItems =
            view.findViewById(R.id.tvTotalItems)

        tvTotalValue =
            view.findViewById(R.id.tvTotalValue)

        // PROFILE
        val sharedPref =
            requireContext().getSharedPreferences(
                "loginPrefs",
                Context.MODE_PRIVATE
            )

        val profileImage =
            sharedPref.getString(
                "profileImage",
                null
            )

        if (!profileImage.isNullOrEmpty()) {

            Glide.with(requireContext())
                .load(profileImage)
                .circleCrop()
                .into(imgDashboardProfile)

            // VIEW PROFILE PHOTO
            imgDashboardProfile.setOnClickListener {

                startActivity(
                    Intent(
                        requireContext(),
                        ProfilePhotoActivity::class.java
                    )
                )
            }
        }

        // GREETING LOGIC
        val hour =
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greeting = when {

            hour in 5..11 ->
                getString(R.string.good_morning)

            hour in 12..16 ->
                getString(R.string.good_afternoon)

            hour in 17..20 ->
                getString(R.string.good_evening)

            else ->
                getString(R.string.working_late)
        }

        tvGreeting.text = "$greeting"

        // ANIMATED TEXTS
        val messages = listOf(

            getString(R.string.msg1),

            getString(R.string.msg2),

            getString(R.string.msg3),

            getString(R.string.msg4)
        )

        var index = 0

        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {

            override fun run() {

                val fadeOut =
                    AlphaAnimation(1f, 0f)

                fadeOut.duration = 500

                val fadeIn =
                    AlphaAnimation(0f, 1f)

                fadeIn.duration = 500

                tvAnimatedText.startAnimation(fadeOut)

                tvAnimatedText.text = messages[index]

                tvAnimatedText.startAnimation(fadeIn)

                index = (index + 1) % messages.size

                handler.postDelayed(this, 2500)
            }
        }

        handler.post(runnable)

        // NOTIFICATION CLICK
        ivNotification.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    NotificationsActivity::class.java
                )
            )
        }


        val actionAdd =
            view.findViewById<LinearLayout>(R.id.actionAdd)

        val actionReports =
            view.findViewById<LinearLayout>(R.id.actionReports)

        val actionAlerts =
            view.findViewById<LinearLayout>(R.id.actionAlerts)

        val actionCategory =
            view.findViewById<LinearLayout>(R.id.actionCategory)

        val actionExport =
            view.findViewById<LinearLayout>(R.id.actionExport)

        val actionSettings =
            view.findViewById<LinearLayout>(R.id.actionSettings)

        ivNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        val tvMoreActions =
            view.findViewById<TextView>(R.id.tvMoreActions)

        val moreActionsLayout =
            view.findViewById<GridLayout>(R.id.moreActionsLayout)

        actionAdd.setOnClickListener {

            startActivity(
                Intent(requireContext(), AddItemActivity::class.java)
            )
        }

        actionReports.setOnClickListener {

            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNav
                )
                .selectedItemId = R.id.nav_reports
        }

        actionAlerts.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    NotificationsActivity::class.java
                )
            )
        }

        actionCategory.setOnClickListener {

            openCategory("Grocery")
        }

        actionExport.setOnClickListener {
            showExportDialog()
        }

        actionSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        var isExpanded = false

        tvMoreActions.setOnClickListener {

            if (isExpanded) {

                moreActionsLayout.visibility = View.GONE

                tvMoreActions.text = getString(R.string.more)

            } else {

                moreActionsLayout.visibility = View.VISIBLE

                tvMoreActions.text = getString(R.string.less)
            }

            isExpanded = !isExpanded
        }

        // FAB CLICK
        fabAdd.setOnClickListener {

            val intent = Intent(
                requireContext(),
                AddItemActivity::class.java
            )

            startActivity(intent)
        }

        // ANALYTICS CLICK
        analyticsCard.setOnClickListener {

            val intent = Intent(
                requireContext(),
                AnalyticsActivity::class.java
            )

            startActivity(intent)
        }

        // SCROLL EFFECT
        val nestedScroll =
            view.findViewById<NestedScrollView>(R.id.nestedScroll)

        nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->

            if (scrollY > oldScrollY) {

                fabAdd.shrink()

            } else {

                fabAdd.extend()
            }
        }

        // CATEGORY CLICK
        cardGrocery.setOnClickListener {

            openCategory("Grocery")
        }

        cardElectronics.setOnClickListener {

            openCategory("Electronics")
        }

        cardPersonal.setOnClickListener {

            openCategory("Personal Care")
        }

        cardStationery.setOnClickListener {

            openCategory("Stationery")
        }

        loadItems()
    }

    override fun onResume() {
        super.onResume()

        loadItems()
    }

    override fun onSearch(query: String) {

        adapter.filter(query)
    }

    private fun loadItems() {

        lifecycleScope.launch {

            val items = itemDao.getAllItems()

            val totalItems =
                items.sumOf { it.quantity }

            val totalValue =
                items.sumOf { it.price * it.quantity }


            // LOW STOCK COUNT
            val lowStockCount =
                items.count { it.quantity < 5 }

            val groceryCount =
                items.filter {
                    it.category == "Grocery"
                }.sumOf { it.quantity }

            val tvBadge =
                view?.findViewById<TextView>(R.id.tvBadge)

            tvBadge?.text = lowStockCount.toString()

            adapter.updateData(items)

            tvTotalItems.text =
                totalItems.toString()

            tvTotalValue.text =
                getString(R.string.total_value, totalValue.toInt())

            tvInsight1.text =
                getString(R.string.insight_low_stock, lowStockCount)

            tvInsight2.text =
                getString(R.string.insight_grocery, groceryCount)

            tvInsight3.text =
                if (totalValue > 50000) {
                    getString(R.string.insight_value_high)
                } else {
                    getString(R.string.insight_value_stable)
                }
        }
    }

    private fun openCategory(category: String) {

        val intent = Intent(
            requireContext(),
            CategoryItemsActivity::class.java
        )

        intent.putExtra("category", category)

        startActivity(intent)
    }

    private fun showExportDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export_options, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)

        val btnPdf = dialogView.findViewById<View>(R.id.btnExportPdf)
        val btnExcel = dialogView.findViewById<View>(R.id.btnExportExcel)
        val btnShare = dialogView.findViewById<View>(R.id.btnShareReport)
        val progressBar = dialogView.findViewById<View>(R.id.progressBar)

        lifecycleScope.launch {
            val items = itemDao.getAllItems()
            if (items.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "No items to export", android.widget.Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@launch
            }

            btnPdf.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToPdf(requireContext(), items) { uri ->
                    progressBar.visibility = View.GONE
                    if (uri != null) {
                        android.widget.Toast.makeText(requireContext(), "PDF Exported to Downloads", android.widget.Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
            }

            btnExcel.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToExcel(requireContext(), items) { uri ->
                    progressBar.visibility = View.GONE
                    if (uri != null) {
                        android.widget.Toast.makeText(requireContext(), "Excel Exported to Downloads", android.widget.Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
            }

            btnShare.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToPdf(requireContext(), items) { uri ->
                    progressBar.visibility = View.GONE
                    if (uri != null) {
                        com.example.smartinventarymanagement.utils.ExportHelper.shareFile(requireContext(), uri, "application/pdf")
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}