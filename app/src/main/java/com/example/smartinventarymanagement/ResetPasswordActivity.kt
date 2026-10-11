package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import android.view.inputmethod.EditorInfo

class ResetPasswordActivity : AppCompatActivity() {
    private var busy = false
    private var flowId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AuthUi.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        flowId = intent.getStringExtra("authFlowId")
        val password = findViewById<EditText>(R.id.etNewPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)
        val submit = findViewById<View>(R.id.btnResetPassword)
        confirm.imeOptions = EditorInfo.IME_ACTION_DONE
        confirm.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { submit.performClick(); true } else false
        }
        findViewById<View>(R.id.authBack).setOnClickListener { finish() }
        val proof = AuthFlows.store.get(flowId)
        if (proof?.verified != true || proof.purpose != AuthPurpose.PASSWORD_RESET) {
            AuthUi.message(this, getString(R.string.auth_session_expired))
            submit.isEnabled = false
        }
        submit.setOnClickListener {
            if (busy) return@setOnClickListener
            val newPassword = password.text.toString()
            if (!AuthValidation.isPasswordValid(newPassword)) {
                password.error = getString(R.string.auth_password_rules)
                return@setOnClickListener
            }
            if (newPassword != confirm.text.toString()) {
                confirm.error = getString(R.string.auth_password_mismatch)
                return@setOnClickListener
            }
            // Consume immediately before the write; flags and cached Firebase users cannot authorize it.
            val approved = AuthFlows.store.consume(flowId, AuthPurpose.PASSWORD_RESET)
            if (approved == null) {
                AuthUi.message(this, getString(R.string.auth_session_expired))
                submit.isEnabled = false
                return@setOnClickListener
            }
            busy = true
            submit.isEnabled = false
            password.isEnabled = false
            confirm.isEnabled = false
            findViewById<View>(R.id.authProgress).visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    withContext(NonCancellable + Dispatchers.IO) {
                        // Revoke trust durably first, so interruption cannot leave an old trusted session.
                        check(SessionManager(applicationContext).passwordWasReset(approved.phone))
                        val changed = DatabaseProvider.getDatabase(applicationContext).userDao()
                            .resetPassword(approved.userId, approved.phone, newPassword)
                        check(changed == 1)
                    }
                    password.text.clear()
                    confirm.text.clear()
                    Toast.makeText(this@ResetPasswordActivity, R.string.auth_reset_success, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AuthUi.message(this@ResetPasswordActivity, getString(R.string.auth_reset_retry))
                } finally {
                    busy = false
                    findViewById<View>(R.id.authProgress).visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) AuthFlows.store.cancel(flowId)
        super.onDestroy()
    }
}
