package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseActivity handles the session timeout logic.
 * All activities that require a logged-in session should extend this class.
 */
open class BaseActivity : AppCompatActivity() {

    protected lateinit var sessionManager: SessionManager
    private val logoutHandler = Handler(Looper.getMainLooper())
    
    // Runnable to periodically check if the session has expired
    private val checkSessionRunnable = object : Runnable {
        override fun run() {
            if (sessionManager.isSessionExpired()) {
                logoutUser()
            } else {
                // Check again in 30 seconds
                logoutHandler.postDelayed(this, 30000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
    }

    /**
     * Resets the session timer on any user interaction (touch, click, key press, etc.)
     */
    override fun onUserInteraction() {
        super.onUserInteraction()
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateSession()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isSessionExpired()) {
                logoutUser()
            } else {
                startSessionCheck()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopSessionCheck()
    }

    private fun startSessionCheck() {
        logoutHandler.removeCallbacks(checkSessionRunnable)
        logoutHandler.post(checkSessionRunnable)
    }

    private fun stopSessionCheck() {
        logoutHandler.removeCallbacks(checkSessionRunnable)
    }

    private fun logoutUser() {
        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
