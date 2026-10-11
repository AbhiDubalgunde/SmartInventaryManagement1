package com.example.smartinventarymanagement

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

class SessionManager(context: Context) {

    // Scope trust to this app/signing key, Android user and device. Restoring
    // loginPrefs on another device must not restore its trusted status.
    private val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

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

        // 5 Minutes
        private const val SESSION_TIMEOUT =
            5 * 60 * 1000L

        private const val KEY_TRUSTED_PHONES = "trusted_phones"
    }

    // -----------------------------
    // TRUSTED DEVICES (2FA)
    // -----------------------------

    fun isDeviceTrusted(phone: String): Boolean {
        if (deviceId.isNullOrBlank()) return false
        return prefs.getStringSet(KEY_TRUSTED_PHONES, emptySet())?.contains("$deviceId:$phone") == true
    }

    fun trustDevice(phone: String) {
        if (deviceId.isNullOrBlank()) return
        val trusted = prefs.getStringSet(KEY_TRUSTED_PHONES, emptySet())?.toMutableSet() ?: mutableSetOf()
        trusted.add("$deviceId:$phone")
        prefs.edit().putStringSet(KEY_TRUSTED_PHONES, trusted).apply()
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

    fun completeLogin(phone: String, fullName: String) {
        val differentUser = prefs.getString("savedUsername", null) != phone
        val editor = prefs.edit()
            .putString("savedUsername", phone)
            .putString("fullName", fullName)
            .putString("userName", fullName)
            .remove("savedPassword")
        if (differentUser) {
            editor.putBoolean(KEY_BIOMETRIC_ENABLED, false)
                .putBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, false)
                .remove("profileImage")
        }
        editor.apply()
        login()
    }

    fun passwordWasReset(phone: String): Boolean {
        val trusted = prefs.getStringSet(KEY_TRUSTED_PHONES, emptySet())?.toMutableSet() ?: mutableSetOf()
        trusted.removeAll { it == phone || it.endsWith(":$phone") }
        val editor = prefs.edit().putStringSet(KEY_TRUSTED_PHONES, trusted).remove("savedPassword")
        if (prefs.getString("savedUsername", null) == phone) {
            editor.putBoolean(KEY_BIOMETRIC_ENABLED, false)
                .putBoolean(KEY_BIOMETRIC_PROMPT_SHOWN, false)
        }
        return editor.putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_LAST_ACTIVE).commit()
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
