package com.example.smartinventarymanagement

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuthException

object AuthUi {
    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences("loginPrefs", Activity.MODE_PRIVATE)
        if (prefs.contains("darkMode")) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                if (prefs.getBoolean("darkMode", false)) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun message(activity: Activity, message: String?) {
        activity.findViewById<TextView>(R.id.authError).apply {
            text = message.orEmpty()
            visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    fun firebaseError(error: Exception?): String {
        if (error is com.google.firebase.FirebaseTooManyRequestsException)
            return "Too many attempts. Please wait before trying again."
        if (error is com.google.firebase.FirebaseNetworkException)
            return "Could not connect. Check your internet connection and try again."
        val code = (error as? FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_OPERATION_NOT_ALLOWED" -> "Phone verification is unavailable. Please contact support. [$code]"
            "ERROR_BILLING_NOT_ENABLED" -> "SMS service is unavailable. Please contact support. [$code]"
            "ERROR_INVALID_PHONE_NUMBER" -> "Enter a valid phone number with country code, for example +919876543210."
            "ERROR_INVALID_VERIFICATION_CODE" -> "That code is incorrect. Check the SMS and try again."
            "ERROR_SESSION_EXPIRED", "ERROR_INVALID_VERIFICATION_ID" -> "This code has expired. Request a new code."
            "ERROR_TOO_MANY_REQUESTS", "ERROR_QUOTA_EXCEEDED" -> "Too many attempts. Please wait before trying again."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Could not connect. Check your internet connection and try again."
            else -> "Phone verification failed. Please try again." +
                if (code != null) " [$code]" else ""
        }
    }
}
