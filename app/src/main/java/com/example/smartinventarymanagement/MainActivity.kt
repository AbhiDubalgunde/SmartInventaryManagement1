package com.example.smartinventarymanagement

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : BaseActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ LANGUAGE APPLY (Must be before super.onCreate)
        val sharedPref = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val language = sharedPref.getString("language", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // ✅ DARK MODE APPLY
        val isDarkMode = sharedPref.getBoolean("darkMode", true)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ensure session is updated on entry
        sessionManager.updateSession()

        bottomNav = findViewById(R.id.bottomNav)

        // DEFAULT SCREEN
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // BOTTOM NAVIGATION
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchScreenFragment())
                R.id.nav_reports -> loadFragment(ReportsFragment())
                R.id.nav_account -> loadFragment(AccountFragment())
            }
            true
        }

        // BACK BUTTON
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (bottomNav.selectedItemId) {
                    R.id.nav_search, R.id.nav_reports, R.id.nav_account -> {
                        bottomNav.selectedItemId = R.id.nav_home
                    }
                    R.id.nav_home -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(getString(R.string.exit_app))
                            .setMessage(getString(R.string.exit_confirm))
                            .setPositiveButton(getString(R.string.yes)) { _, _ -> finish() }
                            .setNegativeButton(getString(R.string.no), null)
                            .show()
                    }
                    else -> finish()
                }
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, fragment)
            .commit()
    }
}
