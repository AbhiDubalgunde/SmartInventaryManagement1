package com.example.smartinventarymanagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * SmsBroadcastReceiver listens for the SMS_RETRIEVED_ACTION from Google Play Services.
 * This is part of the SMS User Consent API flow.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    private var smsBroadcastReceiverListener: SmsBroadcastReceiverListener? = null

    fun setListener(listener: SmsBroadcastReceiverListener) {
        this.smsBroadcastReceiverListener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // ===========================
        // DEBUG POINT 23
        // onReceive()
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 23 : onReceive()")

        // PLACE BREAKPOINT HERE
        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            // ===========================
            // DEBUG POINT 24
            // SMS_RETRIEVED_ACTION detected
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 24 : SMS_RETRIEVED_ACTION detected")

            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // ===========================
                    // DEBUG POINT 25
                    // Status Success
                    // ===========================
                    Log.d("OTP_DEBUG", "DEBUG POINT 25 : Status Success")

                    // PLACE BREAKPOINT HERE
                    // Get the consent intent from extras
                    val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    
                    // ===========================
                    // DEBUG POINT 26
                    // Consent Intent extracted
                    // ===========================
                    Log.d("OTP_DEBUG", "DEBUG POINT 26 : Consent Intent extracted")

                    // ===========================
                    // DEBUG POINT 27
                    // Listener.onSuccess()
                    // ===========================
                    Log.d("OTP_DEBUG", "DEBUG POINT 27 : Listener.onSuccess()")

                    // PLACE BREAKPOINT HERE
                    smsBroadcastReceiverListener?.onSuccess(consentIntent)
                }
                CommonStatusCodes.TIMEOUT -> {
                    smsBroadcastReceiverListener?.onFailure()
                }
            }
        }
    }

    interface SmsBroadcastReceiverListener {
        fun onSuccess(intent: Intent?)
        fun onFailure()
    }
}