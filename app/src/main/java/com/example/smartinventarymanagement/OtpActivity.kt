package com.example.smartinventarymanagement

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.regex.Pattern

class OtpActivity : AppCompatActivity() {

    private val TAG = "OTP_DEBUG"

    private lateinit var tvOtpMessage: TextView
    private lateinit var etOtp: TextInputEditText
    private lateinit var btnVerifyOtp: MaterialButton
    private var generatedOtp: String? = null
    private var phoneNumber: String? = null

    private lateinit var smsBroadcastReceiver: SmsBroadcastReceiver
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        Log.d(TAG, "OtpActivity: onCreate called")

        tvOtpMessage = findViewById(R.id.tvOtpMessage)
        etOtp = findViewById(R.id.etOtp)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)

        phoneNumber = intent.getStringExtra("phone")
        generatedOtp = intent.getStringExtra("otp")

        tvOtpMessage.text = getString(R.string.otp_sent_to, phoneNumber)

        setupSmsUserConsent()

        btnVerifyOtp.setOnClickListener {
            val enteredOtp = etOtp.text.toString().trim()
            if (verifyOtp(enteredOtp)) {
                Toast.makeText(this, getString(R.string.otp_verified), Toast.LENGTH_LONG).show()
                finish() // Go back to where we came from or Dashboard
            } else {
                Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSmsUserConsent() {
        Log.d(TAG, "OtpActivity: setupSmsUserConsent called")
        
        // Initialize the Activity Result Launcher
        intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "OtpActivity: ActivityResultLauncher result code: ${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                // The user allowed the permission, so we get the SMS message from the intent
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                Log.d(TAG, "OtpActivity: SMS message received from consent: $message")
                message?.let { extractOtp(it) }
            }
        }

        // Start listening for SMS User Consent
        SmsRetriever.getClient(this).startSmsUserConsent(null)
            .addOnSuccessListener {
                Log.d(TAG, "OtpActivity: startSmsUserConsent successfully started")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OtpActivity: startSmsUserConsent failed", e)
            }

        // Initialize and setup the BroadcastReceiver
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver.setListener(object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
            override fun onSuccess(intent: Intent?) {
                Log.d(TAG, "OtpActivity: Broadcast SUCCESS received, launching consent intent")
                intent?.let { intentLauncher.launch(it) }
            }

            override fun onFailure() {
                Log.d(TAG, "OtpActivity: Broadcast FAILURE/TIMEOUT")
            }
        })
    }

    /**
     * Extracts a 6-digit OTP from the message using Regex.
     */
    private fun extractOtp(message: String) {
        Log.d(TAG, "OtpActivity: extractOtp called with message: $message")
        val pattern = Pattern.compile("(\\d{6})")
        val matcher = pattern.matcher(message)
        if (matcher.find()) {
            val otp = matcher.group(0)
            Log.d(TAG, "OtpActivity: Regex extracted OTP: $otp")
            etOtp.setText(otp)
        } else {
            Log.w(TAG, "OtpActivity: Regex failed to find 6 consecutive digits")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OtpActivity: onStart - Registering receiver")
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        
        // Register receiver with proper flags for Android 13+ and Google Play Services permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                smsBroadcastReceiver, 
                intentFilter, 
                SmsRetriever.SEND_PERMISSION, 
                null, 
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                smsBroadcastReceiver, 
                intentFilter, 
                SmsRetriever.SEND_PERMISSION, 
                null
            )
        }
        Log.d(TAG, "OtpActivity: Receiver registered")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "OtpActivity: onStop - Unregistering receiver")
        unregisterReceiver(smsBroadcastReceiver)
    }

    /**
     * Verifies the entered OTP against the generated one.
     * Future-ready: Will be replaced by a backend API call or Firebase verification.
     */
    private fun verifyOtp(enteredOtp: String): Boolean {
        return enteredOtp == generatedOtp
    }
}