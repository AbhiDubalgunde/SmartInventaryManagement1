package com.example.smartinventarymanagement

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    private var biometricFailCount = 0
    private var biometricPrompt: BiometricPrompt? = null
    private var biometricPhone: String? = null
    private var busy = false

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

        AuthUi.applyTheme(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
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

        biometricPhone = sharedPreferences.getString("savedUsername", null)
        etUsername.setText(biometricPhone)
        etUsername.isEnabled = true
        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        etUsername.doAfterTextChanged {
            biometricPrompt?.cancelAuthentication()
            updateBiometricOption()
        }
        updateBiometricOption()
        etPassword.imeOptions = EditorInfo.IME_ACTION_DONE
        etPassword.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { btnLogin.performClick(); true } else false
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
            if (busy) return@setOnClickListener
            val phone = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            AuthUi.message(this, null)
            if (!AuthValidation.isPhoneValid(phone)) {
                etUsername.error = getString(R.string.auth_phone_error)
                etUsername.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = getString(R.string.auth_password_required)
                return@setOnClickListener
            }
            setBusy(true)
            lifecycleScope.launch {
                try {
                    val user = DatabaseProvider.getDatabase(this@LoginActivity).userDao().login(phone, password)
                    if (user == null) {
                        AuthUi.message(this@LoginActivity, getString(R.string.auth_credentials_error))
                    } else if (sessionManager.isDeviceTrusted(phone)) {
                        sessionManager.completeLogin(user.phoneNumber, user.fullName)
                        navigateToMain()
                    } else {
                        send2faVerificationCode(user)
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AuthUi.message(this@LoginActivity, getString(R.string.auth_database_error))
                } finally { setBusy(false) }
            }
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        findViewById<View>(R.id.authProgress).visibility = if (value) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !value
        etUsername.isEnabled = !value
        etPassword.isEnabled = !value
        tvCreateAccount.isEnabled = !value
        findViewById<View>(R.id.tvForgotPassword).isEnabled = !value
        btnLogin.setText(if (value) R.string.auth_signing_in else R.string.auth_sign_in)
        updateBiometricOption()
    }

    private fun updateBiometricOption() {
        val available = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        val show = available && sessionManager.isBiometricEnabled() &&
            !biometricPhone.isNullOrBlank() && etUsername.text.toString().trim() == biometricPhone
        btnBiometricRetry.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.biometricLabel).visibility = if (show) View.VISIBLE else View.GONE
        btnBiometricRetry.isEnabled = !busy
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

    private fun send2faVerificationCode(user: User) {
        val pending = AuthFlows.store.begin(user.id, user.phoneNumber, user.fullName, AuthPurpose.LOGIN)
        startActivity(Intent(this, OtpActivity::class.java).apply {
            putExtra("authFlowId", pending.id)
            putExtra("phone", user.phoneNumber)
            putExtra("isFirebaseFlow", true)
            putExtra("is2faFlow", true)
        })
    }

    private fun checkAndShowBiometricPrompt() {
        if (busy || !sessionManager.isBiometricEnabled() || biometricPhone.isNullOrBlank() ||
            etUsername.text.toString().trim() != biometricPhone) return
        val biometricManager = BiometricManager.from(this)
        
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "Biometric hardware not available", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware is currently unavailable", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometrics enrolled", Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
            }
            else -> {
                // If biometric is not available (e.g. no hardware or not set up), 
                // keep password login available
                etPassword.requestFocus()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (!sessionManager.isBiometricEnabled() ||
                        etUsername.text.toString().trim() != biometricPhone) return
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
