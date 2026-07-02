package com.example.smartinventarymanagement

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.Locale
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager
    private var biometricFailCount = 0
    private var biometricPrompt: BiometricPrompt? = null

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvCreateAccount: TextView
    private lateinit var btnBiometricRetry: android.widget.ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ LANGUAGE APPLY
        val pref =
            getSharedPreferences(
                "loginPrefs",
                MODE_PRIVATE
            )

        val language =
            pref.getString(
                "language",
                "en"
            )

        val locale =
            Locale(language!!)

        Locale.setDefault(locale)

        val config =
            Configuration()

        config.setLocale(locale)

        resources.updateConfiguration(
            config,
            resources.displayMetrics
        )

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(
            "loginPrefs",
            MODE_PRIVATE
        )
        sessionManager = SessionManager(this)

        etUsername =
            findViewById<EditText>(R.id.etUsername)

        etPassword =
            findViewById<EditText>(R.id.etPassword)

        btnLogin =
            findViewById<Button>(R.id.btnLogin)

        tvCreateAccount =
            findViewById<TextView>(R.id.tvCreateAccount)

        btnBiometricRetry = findViewById(R.id.btnBiometricRetry)

        val tvWelcomeBack = findViewById<TextView>(R.id.tvWelcomeBack)
        val tvLoginSubtitle = findViewById<TextView>(R.id.tvLoginSubtitle)

        val isLoggedIn = sessionManager.isLoggedIn()
        val biometricEnabled = sessionManager.isBiometricEnabled()
        val savedUsername = sharedPreferences.getString("savedUsername", "")

        if (isLoggedIn || (biometricEnabled && !savedUsername.isNullOrEmpty())) {
            // ✅ SECURITY LOCK MODE (Session expired or Biometric enabled)
            tvWelcomeBack.visibility = TextView.VISIBLE
            tvLoginSubtitle.visibility = TextView.VISIBLE
            
            etUsername.setText(savedUsername)
            etUsername.isEnabled = false // Lock username

            tvCreateAccount.visibility = TextView.GONE // Hide registration

            if (biometricEnabled) {
                btnBiometricRetry.visibility = TextView.VISIBLE
                checkAndShowBiometricPrompt()
            }
        }


        // OPEN REGISTER
        tvCreateAccount.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        btnBiometricRetry.setOnClickListener {
            checkAndShowBiometricPrompt()
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedUsername = sharedPreferences.getString("savedUsername", null)
            val savedPassword = sharedPreferences.getString("savedPassword", null)

            if (username == savedUsername && password == savedPassword) {
                navigateToMain()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(etPassword, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun navigateToMain() {
        // Double check session update before moving
        sessionManager.login()
        // Reset the "prompt shown" flag when a user successfully logs in, 
        // so they might be prompted again if they haven't enabled it yet (optional logic)
        // or just let HomeFragment handle the logic.
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkAndShowBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Biometric hardware not available", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware is currently unavailable", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometrics enrolled", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            else -> {
                // If biometric is not available (e.g. no hardware or not set up), 
                // fallback to main activity if already logged in
                navigateToMain()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    biometricFailCount = 0
                    Toast.makeText(this@LoginActivity, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels or too many system errors, we stay on login screen for password
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@LoginActivity, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
                    }
                    // Focus password field on error/cancel
                    etPassword.requestFocus()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    biometricFailCount++
                    Toast.makeText(this@LoginActivity, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    if (biometricFailCount >= 3) {
                        biometricPrompt?.cancelAuthentication()
                        Toast.makeText(this@LoginActivity, "Too many attempts. Use Password.", Toast.LENGTH_LONG).show()

                        // Focus password field and show keyboard
                        etPassword.requestFocus()
                        showKeyboard()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Use your fingerprint to login")
            .setNegativeButtonText("Use Password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt?.authenticate(promptInfo)
    }
}