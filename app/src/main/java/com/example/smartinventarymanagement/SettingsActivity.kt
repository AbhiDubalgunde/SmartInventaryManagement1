package com.example.smartinventarymanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val btnLanguage = findViewById<LinearLayout>(R.id.btnLanguage)
        val tvLanguageDisplay = findViewById<TextView>(R.id.tvLanguage)
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // LANGUAGE DISPLAY
        val currentLang = sharedPreferences.getString("language", "en")
        tvLanguageDisplay.text = if (currentLang == "kn") getString(R.string.kannada) else getString(R.string.english)

        // DARK MODE
        val isDarkMode = sharedPreferences.getBoolean("darkMode", true)
        switchDarkMode.isChecked = isDarkMode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // LANGUAGE SELECTION
        btnLanguage.setOnClickListener {
            val languages = arrayOf(
                getString(R.string.english),
                getString(R.string.kannada)
            )

            AlertDialog.Builder(this)
                .setTitle(R.string.choose_language)
                .setItems(languages) { _, which ->
                    when (which) {
                        0 -> setLocale("en")
                        1 -> setLocale("kn")
                    }
                }
                .show()
        }

        // LOGOUT
        btnLogout.setOnClickListener {
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setLocale(languageCode: String) {
        sharedPreferences.edit().putString("language", languageCode).apply()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Recreate activity and return to main to apply language project-wide
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
