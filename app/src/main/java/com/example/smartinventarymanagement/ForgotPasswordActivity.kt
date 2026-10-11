package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AuthUi.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        val phoneInput = findViewById<EditText>(R.id.etRecoveryPhone)
        val submit = findViewById<View>(R.id.btnRecovery)
        findViewById<View>(R.id.authBack).setOnClickListener { finish() }
        phoneInput.imeOptions = EditorInfo.IME_ACTION_DONE
        phoneInput.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { submit.performClick(); true } else false
        }
        submit.setOnClickListener {
            if (busy) return@setOnClickListener
            val phone = phoneInput.text.toString().trim()
            AuthUi.message(this, null)
            if (!AuthValidation.isPhoneValid(phone)) {
                phoneInput.error = getString(R.string.auth_phone_error)
                return@setOnClickListener
            }
            busy = true
            submit.isEnabled = false
            phoneInput.isEnabled = false
            findViewById<View>(R.id.authProgress).visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val user = DatabaseProvider.getDatabase(this@ForgotPasswordActivity).userDao().getUserByPhone(phone)
                    if (user == null) {
                        AuthUi.message(this@ForgotPasswordActivity, getString(R.string.auth_unknown_phone))
                    } else {
                        val flow = AuthFlows.store.begin(user.id, user.phoneNumber, user.fullName, AuthPurpose.PASSWORD_RESET)
                        startActivity(Intent(this@ForgotPasswordActivity, OtpActivity::class.java).apply {
                            putExtra("authFlowId", flow.id)
                            putExtra("phone", user.phoneNumber)
                            putExtra("isFirebaseFlow", true)
                            putExtra("isPasswordResetFlow", true)
                        })
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AuthUi.message(this@ForgotPasswordActivity, getString(R.string.auth_database_error))
                } finally {
                    busy = false
                    submit.isEnabled = true
                    phoneInput.isEnabled = true
                    findViewById<View>(R.id.authProgress).visibility = View.GONE
                }
            }
        }
    }
}
