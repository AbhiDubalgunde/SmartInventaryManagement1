package com.example.smartinventarymanagement

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import java.util.regex.Pattern
import kotlin.random.Random

class RetrieverDemoActivity : AppCompatActivity(), RetrieverBroadcastReceiver.OtpReceiveListener {

    private var generatedOtp: String = ""
    private var appHash: String = ""
    private var smsReceiver: RetrieverBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // ===========================
        // DEBUG POINT 28
        // Activity Created
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 28 : Activity Created")
        
        super.onCreate(savedInstanceState)
        // Note: Using ViewBinding as requested for Premium UI/Demo if possible, 
        // but the user's project had viewBinding disabled in a previous turn.
        // Let's check build.gradle.kts again or use findViewById.
        // I will use findViewById to be safe since viewBinding was recently disabled.
        setContentView(R.layout.activity_retriever_demo)

        setupToolbar()
        loadAppHash()
        generateNewOtp()
        setupListeners()
        addLog("✔ Demo Initialized")
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadAppHash() {
        try {
            val hashHelper = RetrieverHashHelper(this)
            val hashes = hashHelper.getAppHashes()
            if (hashes.isNotEmpty()) {
                // ===========================
                // DEBUG POINT 30
                // App Hash Generated
                // ===========================
                Log.d("OTP_DEBUG", "DEBUG POINT 30 : App Hash Generated")
                
                // PLACE BREAKPOINT HERE
                appHash = hashes[0]
                findViewById<android.widget.TextView>(R.id.tvAppHash).text = appHash
                updateSmsTemplate()
            }
        } catch (e: Exception) {
            addLog("✘ Error loading Hash: ${e.message}")
        }
    }

    private fun generateNewOtp() {
        // ===========================
        // DEBUG POINT 29
        // Generate OTP
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 29 : Generate OTP")
        
        // PLACE BREAKPOINT HERE
        generatedOtp = Random.nextInt(100000, 999999).toString()
        findViewById<android.widget.TextView>(R.id.tvDemoOtp).text = generatedOtp
        updateSmsTemplate()
        addLog("✔ New OTP Generated: $generatedOtp")
    }

    private fun updateSmsTemplate() {
        val template = "Your Smart Inventory OTP is $generatedOtp\n$appHash"
        findViewById<android.widget.TextView>(R.id.tvSmsTemplate).text = template
    }

    private fun setupListeners() {
        findViewById<android.widget.Button>(R.id.btnGenerateDemoOtp).setOnClickListener {
            generateNewOtp()
        }

        findViewById<android.widget.Button>(R.id.btnCopyHash).setOnClickListener {
            // ===========================
            // DEBUG POINT 31
            // Copy Hash
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 31 : Copy Hash")
            
            // PLACE BREAKPOINT HERE
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("App Hash", appHash)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Hash Copied!", Toast.LENGTH_SHORT).show()
            addLog("✔ Hash copied to clipboard")
        }

        findViewById<android.widget.Button>(R.id.btnStartListening).setOnClickListener {
            startSmsRetriever()
        }
    }

    private fun startSmsRetriever() {
        // ===========================
        // DEBUG POINT 32
        // Start Retriever
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 32 : Start Retriever")
        
        // PLACE BREAKPOINT HERE
        addLog("⏳ Starting SmsRetriever...")
        findViewById<android.widget.TextView>(R.id.tvRetrieverStatus).text = "🟢 Waiting for SMS..."
        
        val client = SmsRetriever.getClient(this)
        val task = client.startSmsRetriever()

        task.addOnSuccessListener {
            addLog("✔ SmsRetriever Started Successfully")
            registerReceiver()
        }

        task.addOnFailureListener { e ->
            addLog("✘ Failed to start Retriever: ${e.message}")
            findViewById<android.widget.TextView>(R.id.tvRetrieverStatus).text = "🔴 Failed"
        }
    }

    private fun registerReceiver() {
        if (smsReceiver == null) {
            smsReceiver = RetrieverBroadcastReceiver()
            smsReceiver?.setOtpListener(this)
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null)
        addLog("✔ BroadcastReceiver Registered")
    }

    override fun onOtpReceived(message: String?) {
        // ===========================
        // DEBUG POINT 33
        // Broadcast Received
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 33 : Broadcast Received")
        
        // PLACE BREAKPOINT HERE
        addLog("✔ Broadcast Received")
        findViewById<android.widget.TextView>(R.id.tvReceivedSms).text = message
        
        if (message != null) {
            extractOtp(message)
        }
    }

    private fun extractOtp(message: String) {
        // ===========================
        // DEBUG POINT 34
        // Regex Started
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 34 : Regex Started")
        
        // PLACE BREAKPOINT HERE
        addLog("⏳ Running Regex Match...")
        val pattern = Pattern.compile("(\\d{6})")
        val matcher = pattern.matcher(message)
        
        if (matcher.find()) {
            // ===========================
            // DEBUG POINT 35
            // OTP Extracted
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 35 : OTP Extracted")
            
            val otp = matcher.group(0)
            addLog("✔ Regex Success: OTP Extracted ($otp)")
            
            // ===========================
            // DEBUG POINT 36
            // OTP Auto Filled
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 36 : OTP Auto Filled")
            
            // PLACE BREAKPOINT HERE
            findViewById<android.widget.EditText>(R.id.etDemoAutoFill).setText(otp)
            addLog("✔ OTP Filled Automatically")
            
            verifyOtp(otp)
        } else {
            addLog("✘ Regex Failed: No 6-digit OTP found")
        }
    }

    private fun verifyOtp(otp: String?) {
        if (otp == generatedOtp) {
            // ===========================
            // DEBUG POINT 37
            // Verification Success
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 37 : Verification Success")
            
            // PLACE BREAKPOINT HERE
            addLog("✔ Verification Success!")
            findViewById<android.widget.TextView>(R.id.tvRetrieverStatus).text = "✅ Success"
            Toast.makeText(this, "OTP Verified Successfully!", Toast.LENGTH_LONG).show()
        } else {
            addLog("✘ Verification Failed: OTP Mismatch")
            findViewById<android.widget.TextView>(R.id.tvRetrieverStatus).text = "❌ Mismatch"
        }
    }

    override fun onOtpTimeout() {
        addLog("✘ SMS Retrieval Timeout (5 minutes)")
        findViewById<android.widget.TextView>(R.id.tvRetrieverStatus).text = "⏰ Timeout"
    }

    private fun addLog(message: String) {
        val tvLogs = findViewById<android.widget.TextView>(R.id.tvDemoLogs)
        val currentLogs = tvLogs.text.toString()
        val newLogs = "$message\n$currentLogs"
        tvLogs.text = newLogs
        Log.d("RetrieverDemo", message)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver)
                addLog("✔ Receiver Unregistered")
            } catch (e: Exception) {
                // Already unregistered
            }
        }
    }
}
