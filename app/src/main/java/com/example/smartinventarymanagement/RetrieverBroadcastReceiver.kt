package com.example.smartinventarymanagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * BroadcastReceiver for the SMS Retriever Demo.
 * Isolated from the production SMS User Consent receiver.
 */
class RetrieverBroadcastReceiver : BroadcastReceiver() {

    private var listener: OtpReceiveListener? = null

    fun setOtpListener(listener: OtpReceiveListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                    listener?.onOtpReceived(message)
                }
                CommonStatusCodes.TIMEOUT -> {
                    listener?.onOtpTimeout()
                }
            }
        }
    }

    interface OtpReceiveListener {
        fun onOtpReceived(message: String?)
        fun onOtpTimeout()
    }
}
