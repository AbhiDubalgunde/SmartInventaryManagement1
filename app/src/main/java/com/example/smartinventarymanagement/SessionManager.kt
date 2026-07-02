package com.example.smartinventarymanagement

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "loginPrefs",
            Context.MODE_PRIVATE
        )

    companion object {

        private const val KEY_LAST_ACTIVE =
            "lastActiveTime"

        private const val KEY_IS_LOGGED_IN =
            "isLoggedIn"

        private const val KEY_BIOMETRIC_ENABLED =
            "biometricEnabled"

        private const val KEY_BIOMETRIC_PROMPT_SHOWN =
            "biometricPromptShown"

        // 10 Minutes
        private const val SESSION_TIMEOUT =
            10 * 60 * 1000L
    }

    // -----------------------------
    // LOGIN
    // -----------------------------

    fun login() {

        prefs.edit()

            .putBoolean(
                KEY_IS_LOGGED_IN,
                true
            )

            .putLong(
                KEY_LAST_ACTIVE,
                System.currentTimeMillis()
            )

            .apply()
    }

    // -----------------------------
    // LOGOUT
    // -----------------------------

    fun logout() {

        prefs.edit()

            .putBoolean(
                KEY_IS_LOGGED_IN,
                false
            )

            .remove(KEY_LAST_ACTIVE)

            .apply()
    }

    // -----------------------------
    // CHECK LOGIN
    // -----------------------------

    fun isLoggedIn(): Boolean {

        return prefs.getBoolean(
            KEY_IS_LOGGED_IN,
            false
        )
    }

    // -----------------------------
    // BIOMETRIC
    // -----------------------------

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun wasBiometricPromptShown(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, false)
    }

    fun setBiometricPromptShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, shown).apply()
    }

    // -----------------------------
    // UPDATE SESSION
    // -----------------------------

    fun updateSession() {

        prefs.edit()

            .putLong(
                KEY_LAST_ACTIVE,
                System.currentTimeMillis()
            )

            .apply()
    }

    // -----------------------------
    // CHECK SESSION
    // -----------------------------

    fun isSessionExpired(): Boolean {

        if (!isLoggedIn()) {

            return true
        }

        val lastActive =

            prefs.getLong(
                KEY_LAST_ACTIVE,
                0L
            )

        if (lastActive == 0L) {

            return true
        }

        val currentTime =
            System.currentTimeMillis()

        return currentTime - lastActive >
                SESSION_TIMEOUT
    }
}