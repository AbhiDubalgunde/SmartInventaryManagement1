package com.example.smartinventarymanagement

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.Calendar
import java.util.zip.ZipInputStream

class HomeFragment : Fragment(), SearchFragment.OnSearchListener {

    private lateinit var db: AppDatabase
    private lateinit var itemDao: ItemDao
    private lateinit var adapter: ItemAdapter
    private var isFirstLaunch = true

    private val restoreLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { restoreBackup(it) }
        }

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalValue: TextView
    private lateinit var tvInsight1: TextView
    private lateinit var tvInsight2: TextView
    private lateinit var tvInsight3: TextView
    private lateinit var emptyStateLayout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseProvider.getDatabase(requireContext())
        itemDao = db.itemDao()

        // Setup UI Components
        setupSearch()
        setupRecyclerView(view)
        setupViews(view)
        setupListeners(view)
        
        loadItems()
        checkBiometricSetup()

        if (isFirstLaunch) {
            runEntranceAnimations(view)
            isFirstLaunch = false
        }
    }

    private fun setupSearch() {
        childFragmentManager.beginTransaction()
            .replace(R.id.searchContainer, SearchFragment())
            .commit()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        adapter = ItemAdapter(emptyList(),
            onDeleteClick = { item -> showDeleteDialog(item) },
            onItemClick = { item -> openEditItem(item) }
        )
        recyclerView.adapter = adapter
    }

    private fun setupViews(view: View) {
        tvInsight1 = view.findViewById(R.id.tvInsight1)
        tvInsight2 = view.findViewById(R.id.tvInsight2)
        tvInsight3 = view.findViewById(R.id.tvInsight3)
        tvTotalItems = view.findViewById(R.id.tvTotalItems)
        tvTotalValue = view.findViewById(R.id.tvTotalValue)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
    }

    private fun setupListeners(view: View) {
        val fabAdd = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)
        val imgProfile = view.findViewById<ImageView>(R.id.imgDashboardProfile)
        val ivNotification = view.findViewById<ImageView>(R.id.ivNotification)
        
        // Correct view types to avoid ClassCastException
        val cardGrocery = view.findViewById<CardView>(R.id.cardGrocery)
        val cardElectronics = view.findViewById<CardView>(R.id.cardElectronics)
        val cardPersonal = view.findViewById<CardView>(R.id.cardPersonal)
        val cardStationery = view.findViewById<CardView>(R.id.cardStationery)
        val cardTotalItems = view.findViewById<CardView>(R.id.cardTotalItems)
        val cardTotalValue = view.findViewById<CardView>(R.id.cardTotalValue)

        // Apply Click Listeners
        cardTotalItems.setOnClickListener { openAnalytics("items") }
        cardTotalValue.setOnClickListener { openAnalytics("value") }
        cardGrocery.setOnClickListener { openCategory("Grocery") }
        cardElectronics.setOnClickListener { openCategory("Electronics") }
        cardPersonal.setOnClickListener { openCategory("Personal Care") }
        cardStationery.setOnClickListener { openCategory("Stationery") }
        
        // Press Animations
        applyPressAnimation(cardTotalItems)
        applyPressAnimation(cardTotalValue)
        applyPressAnimation(cardGrocery)
        applyPressAnimation(cardElectronics)
        applyPressAnimation(cardPersonal)
        applyPressAnimation(cardStationery)

        fabAdd.setOnClickListener { startActivity(Intent(requireContext(), AddItemActivity::class.java)) }
        ivNotification.setOnClickListener { startActivity(Intent(requireContext(), NotificationsActivity::class.java)) }

        // Quick Actions
        val actionAdd = view.findViewById<View>(R.id.actionAdd)
        val actionReports = view.findViewById<View>(R.id.actionReports)
        val actionAlerts = view.findViewById<View>(R.id.actionAlerts)
        val actionCategory = view.findViewById<View>(R.id.actionCategory)
        val actionAI = view.findViewById<View>(R.id.actionAI)
        val actionAdmin = view.findViewById<View>(R.id.actionAdmin)
        val btnSeeAll = view.findViewById<View>(R.id.btnSeeAll)

        btnSeeAll.setOnClickListener {
            val intent = Intent(requireContext(), CategoryItemsActivity::class.java)
            intent.putExtra("category", getString(R.string.all_items))
            startActivity(intent)
        }

        actionAdd.setOnClickListener { startActivity(Intent(requireContext(), AddItemActivity::class.java)) }
        actionReports.setOnClickListener { 
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_reports 
        }
        actionAlerts.setOnClickListener { startActivity(Intent(requireContext(), NotificationsActivity::class.java)) }
        actionCategory.setOnClickListener { openCategory("Grocery") }
        actionAI.setOnClickListener { 
            Toast.makeText(requireContext(), getString(R.string.ai_insights_soon), Toast.LENGTH_SHORT).show()
        }
        actionAdmin.setOnClickListener { 
            startActivity(Intent(requireContext(), PhoneNumberActivity::class.java))
        }
        
        view.findViewById<View>(R.id.actionExport).setOnClickListener { showExportDialog() }
        view.findViewById<View>(R.id.actionBackup).setOnClickListener { showBackupOptions() }
        view.findViewById<View>(R.id.actionSettings).setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        view.findViewById<View>(R.id.btnEmptyAdd).setOnClickListener { startActivity(Intent(requireContext(), AddItemActivity::class.java)) }

        // Press animations for quick actions
        applyPressAnimation(actionAdd)
        applyPressAnimation(actionReports)
        applyPressAnimation(actionAlerts)
        applyPressAnimation(actionCategory)
        applyPressAnimation(actionAI)
        applyPressAnimation(view.findViewById(R.id.actionExport))
        applyPressAnimation(view.findViewById(R.id.actionBackup))
        applyPressAnimation(view.findViewById(R.id.actionSettings))

        // More Actions Expansion
        val tvMoreActions = view.findViewById<TextView>(R.id.tvMoreActions)
        val moreActionsLayout = view.findViewById<GridLayout>(R.id.moreActionsLayout)
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

        // Profile & Greeting
        setupProfile(imgProfile, view.findViewById(R.id.tvGreeting), view.findViewById(R.id.tvHeyText))
        setupAnimatedText(view.findViewById(R.id.tvAnimatedText))

        // Scroll Effect
        val nestedScroll = view.findViewById<NestedScrollView>(R.id.nestedScroll)
        nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) fabAdd.shrink() else fabAdd.extend()
        }
    }

    private fun applyPressAnimation(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                    }
                }
            }
            true
        }
    }

    private fun runEntranceAnimations(root: View) {
        val header = root.findViewById<View>(R.id.headerLayout)
        val summaryCards = root.findViewById<View>(R.id.analyticsCard)
        val quickActionsGrid = root.findViewById<GridLayout>(R.id.quickActionsGrid)
        val searchBar = root.findViewById<View>(R.id.searchContainer)
        val insightsCard = root.findViewById<View>(R.id.insightsCard)
        val recentLabel = root.findViewById<View>(R.id.recentLabel)
        val btnSeeAll = root.findViewById<View>(R.id.btnSeeAll)
        val recyclerView = root.findViewById<View>(R.id.recyclerView)
        val categoriesLabel = root.findViewById<View>(R.id.categoriesLabel)
        val categoriesGrid = root.findViewById<GridLayout>(R.id.categoriesGrid)

        val interp = DecelerateInterpolator()

        // 1. Header fade + slide down
        header.alpha = 0f
        header.translationY = -50f
        header.animate().alpha(1f).translationY(0f).setDuration(400).setInterpolator(interp).start()

        // 2. Summary Cards fade + scale
        summaryCards.alpha = 0f
        summaryCards.scaleX = 0.9f
        summaryCards.scaleY = 0.9f
        summaryCards.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(150).setDuration(350).setInterpolator(interp).start()

        // 3. Search Bar
        searchBar.alpha = 0f
        searchBar.animate().alpha(1f).setStartDelay(250).setDuration(300).start()

        // 4. Quick Actions (Staggered)
        for (i in 0 until quickActionsGrid.childCount) {
            val child = quickActionsGrid.getChildAt(i)
            child.alpha = 0f
            child.translationY = 20f
            child.animate().alpha(1f).translationY(0f).setStartDelay(300L + (i * 50)).setDuration(300).start()
        }

        // 5. Smart Insights
        insightsCard.alpha = 0f
        insightsCard.animate().alpha(1f).setStartDelay(500).setDuration(400).start()

        // 6. Recent Items Section
        recentLabel.alpha = 0f
        btnSeeAll.alpha = 0f
        recyclerView.alpha = 0f
        recentLabel.animate().alpha(1f).setStartDelay(550).setDuration(300).start()
        btnSeeAll.animate().alpha(1f).setStartDelay(550).setDuration(300).start()
        recyclerView.animate().alpha(1f).setStartDelay(600).setDuration(400).start()

        // 7. Categories (Staggered)
        categoriesLabel.alpha = 0f
        categoriesLabel.animate().alpha(1f).setStartDelay(650).setDuration(300).start()
        for (i in 0 until categoriesGrid.childCount) {
            val child = categoriesGrid.getChildAt(i)
            child.alpha = 0f
            child.scaleX = 0.9f
            child.scaleY = 0.9f
            child.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(700L + (i * 100)).setDuration(350).setInterpolator(interp).start()
        }
    }

    private fun animateCount(textView: TextView, to: Double, isCurrency: Boolean = false) {
        val animator = ValueAnimator.ofFloat(0f, to.toFloat())
        animator.duration = 700
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            if (isCurrency) {
                textView.text = formatCurrency(value.toDouble())
            } else {
                textView.text = value.toInt().toString()
            }
        }
        animator.start()
    }

    private fun loadItems() {
        lifecycleScope.launch {
            val allItems = itemDao.getAllItems().sortedByDescending { it.id }
            val items = allItems.take(4) // Limit to 4 items for the dashboard
            val totalItems = allItems.sumOf { it.quantity }
            val totalValue = allItems.sumOf { it.price * it.quantity }
            val lowStockCount = allItems.count { it.quantity < 5 }
            val groceryCount = allItems.filter { it.category == "Grocery" }.sumOf { it.quantity }

            view?.findViewById<TextView>(R.id.tvBadge)?.text = lowStockCount.toString()
            
            if (allItems.isEmpty()) {
                emptyStateLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateData(items)
            }

            if (isFirstLaunch) {
                animateCount(tvTotalItems, totalItems.toDouble(), false)
                animateCount(tvTotalValue, totalValue, true)
            } else {
                tvTotalItems.text = totalItems.toString()
                tvTotalValue.text = formatCurrency(totalValue)
            }

            tvInsight1.text = getString(R.string.insight_low_stock, lowStockCount)
            tvInsight2.text = getString(R.string.insight_grocery, groceryCount)
            tvInsight3.text = if (totalValue > 50000) getString(R.string.insight_value_high) else getString(R.string.insight_value_stable)
        }
    }

    private fun setupProfile(imgProfile: ImageView, tvGreeting: TextView, tvHey: TextView) {
        val sharedPref = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val profileImage = sharedPref.getString("profileImage", null)
        val userName = sharedPref.getString("userName", "Abhishek")
        
        tvHey.text = getString(R.string.hey_user, userName)

        if (!profileImage.isNullOrEmpty()) {
            Glide.with(requireContext()).load(profileImage).circleCrop().into(imgProfile)
            imgProfile.setOnClickListener { startActivity(Intent(requireContext(), ProfilePhotoActivity::class.java)) }
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when {
            hour in 5..11 -> getString(R.string.good_morning)
            hour in 12..16 -> getString(R.string.good_afternoon)
            hour in 17..20 -> getString(R.string.good_evening)
            else -> getString(R.string.working_late)
        }
    }

    private fun setupAnimatedText(tvAnimatedText: TextView) {
        val messages = listOf(getString(R.string.msg1), getString(R.string.msg2), getString(R.string.msg3), getString(R.string.msg4))
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }
                val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }
                tvAnimatedText.startAnimation(fadeOut)
                tvAnimatedText.text = messages[index]
                tvAnimatedText.startAnimation(fadeIn)
                index = (index + 1) % messages.size
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    private fun formatCurrency(amount: Double): String {
        val df = DecimalFormat("#.#")
        return when {
            amount >= 10000000 -> "₹${df.format(amount / 10000000)}Cr"
            amount >= 100000 -> "₹${df.format(amount / 100000)}L"
            amount >= 1000 -> "₹${df.format(amount / 1000)}K"
            else -> "₹${DecimalFormat("#,##0").format(amount)}"
        }
    }

    private fun openAnalytics(type: String) {
        startActivity(Intent(requireContext(), AnalyticsActivity::class.java).apply { putExtra("analytics_type", type) })
    }

    private fun openCategory(category: String) {
        startActivity(Intent(requireContext(), CategoryItemsActivity::class.java).apply { putExtra("category", category) })
    }

    private fun openEditItem(item: Item) {
        startActivity(Intent(requireContext(), AddItemActivity::class.java).apply {
            putExtra("id", item.id)
            putExtra("name", item.name)
            putExtra("qty", item.quantity)
            putExtra("price", item.price)
            putExtra("category", item.category)
            putExtra("imageUri", item.imageUri)
            putExtra("expiryDate", item.expiryDate)
        })
    }

    private fun showDeleteDialog(item: Item) {
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
    }

    private fun checkBiometricSetup() {
        val sessionManager = SessionManager(requireContext())
        if (sessionManager.isLoggedIn() && !sessionManager.isBiometricEnabled() && !sessionManager.wasBiometricPromptShown()) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.enable_fingerprint_login))
                .setMessage(getString(R.string.biometric_prompt_msg))
                .setPositiveButton(getString(R.string.enable)) { _, _ -> authenticateAndEnableBiometric() }
                .setNegativeButton(getString(R.string.later)) { _, _ -> sessionManager.setBiometricPromptShown(true) }
                .show()
        }
    }

    private fun authenticateAndEnableBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = androidx.biometric.BiometricPrompt(this, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    val sessionManager = SessionManager(requireContext())
                    sessionManager.setBiometricEnabled(true)
                    sessionManager.setBiometricPromptShown(true)
                    Toast.makeText(requireContext(), getString(R.string.biometric_enabled), Toast.LENGTH_SHORT).show()
                }
            })
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.enable_fingerprint_login))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun showBackupOptions() {
        val options = arrayOf(getString(R.string.create_backup), getString(R.string.restore_backup))
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.backup_options))
            .setItems(options) { _, which -> if (which == 0) exportBackup() else restoreLauncher.launch("application/zip") }
            .show()
    }

    private fun exportBackup() {
        lifecycleScope.launch {
            try {
                val items = itemDao.getAllItems()
                val backupDir = File(requireContext().cacheDir, "inventory_backup").apply { if (!exists()) mkdirs() }
                val imagesDir = File(backupDir, "item_images").apply { if (!exists()) mkdirs() }
                val updatedItems = items.map { item ->
                    if (!item.imageUri.isNullOrEmpty()) {
                        try {
                            val oldFile = File(Uri.parse(item.imageUri).path!!)
                            val newFile = File(imagesDir, oldFile.name)
                            oldFile.copyTo(newFile, overwrite = true)
                            item.copy(imageUri = newFile.name)
                        } catch (e: Exception) { item }
                    } else item
                }
                File(backupDir, "backup.json").writeText(Gson().toJson(updatedItems))
                val zipFile = File(requireContext().cacheDir, "inventory_backup.zip")
                zipFolder(backupDir, zipFile)
                val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", zipFile)
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, getString(R.string.share_backup)))
            } catch (e: Exception) { Toast.makeText(requireContext(), getString(R.string.backup_failed), Toast.LENGTH_SHORT).show() }
        }
    }

    private fun restoreBackup(zipUri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(zipUri) ?: return@launch
                val restoreDir = File(requireContext().cacheDir, "restore_temp").apply { if (exists()) deleteRecursively(); mkdirs() }
                val zis = ZipInputStream(inputStream)
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(restoreDir, entry.name)
                    if (entry.isDirectory) file.mkdirs() else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { zis.copyTo(it) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                zis.close()
                val json = File(restoreDir, "backup.json").readText()
                val backedUpItems: List<Item> = Gson().fromJson(json, object : TypeToken<List<Item>>() {}.type)
                val restoredItems = backedUpItems.map { item ->
                    if (!item.imageUri.isNullOrEmpty()) {
                        val imageFile = File(restoreDir, "item_images/${item.imageUri}")
                        if (imageFile.exists()) {
                            val newImageFile = File(requireContext().filesDir, imageFile.name)
                            imageFile.copyTo(newImageFile, overwrite = true)
                            item.copy(imageUri = Uri.fromFile(newImageFile).toString())
                        } else item.copy(imageUri = null)
                    } else item
                }
                itemDao.getAllItems().forEach { itemDao.delete(it) }
                restoredItems.forEach { itemDao.insert(it.copy(id = 0)) }
                Toast.makeText(requireContext(), getString(R.string.backup_restored), Toast.LENGTH_LONG).show()
                loadItems()
            } catch (e: Exception) { Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun zipFolder(sourceFolder: File, zipFile: File) {
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
            sourceFolder.walkTopDown().forEach { file ->
                if (file.isFile) {
                    zos.putNextEntry(java.util.zip.ZipEntry(file.relativeTo(sourceFolder).path))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }

    override fun onResume() { super.onResume(); loadItems() }
    override fun onSearch(query: String) { adapter.filter(query) }

    private fun showExportDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_export_options, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        val progressBar = dialogView.findViewById<View>(R.id.progressBar)
        lifecycleScope.launch {
            val items = itemDao.getAllItems()
            if (items.isEmpty()) { dialog.dismiss(); return@launch }
            dialogView.findViewById<View>(R.id.btnExportPdf).setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToPdf(requireContext(), items) { progressBar.visibility = View.GONE; dialog.dismiss() }
            }
            dialogView.findViewById<View>(R.id.btnExportExcel).setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToExcel(requireContext(), items) { progressBar.visibility = View.GONE; dialog.dismiss() }
            }
            dialogView.findViewById<View>(R.id.btnShareReport).setOnClickListener {
                progressBar.visibility = View.VISIBLE
                com.example.smartinventarymanagement.utils.ExportHelper.exportToPdf(requireContext(), items) { uri ->
                    progressBar.visibility = View.GONE
                    if (uri != null) com.example.smartinventarymanagement.utils.ExportHelper.shareFile(requireContext(), uri, "application/pdf")
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }
}
