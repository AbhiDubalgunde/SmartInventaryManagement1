package com.example.smartinventarymanagement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.Locale

class SettingsActivity : BaseActivity() {

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
        val switchBiometric = findViewById<SwitchCompat>(R.id.switchBiometric)
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

        // BIOMETRIC
        val isBiometricEnabled = sessionManager.isBiometricEnabled()
        switchBiometric.isChecked = isBiometricEnabled
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If turning ON, authenticate first
                authenticateAndEnableBiometric(switchBiometric)
            } else {
                // If turning OFF, just disable
                sessionManager.setBiometricEnabled(false)
                Toast.makeText(this, "Fingerprint Login Disabled", Toast.LENGTH_SHORT).show()
            }
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
            sessionManager.logout()
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
        
        // RECREATE ACTIVITY
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun authenticateAndEnableBiometric(switch: SwitchCompat) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    sessionManager.setBiometricEnabled(true)
                    Toast.makeText(this@SettingsActivity, "Fingerprint Login Enabled Successfully", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    switch.isChecked = false
                    Toast.makeText(this@SettingsActivity, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // We don't necessarily want to uncheck on a single failed attempt, 
                    // but the prompt usually stays up. If it closes, we might need to handle it.
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Login")
            .setSubtitle("Confirm your identity to enable fingerprint login")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
