package com.example.smartinventarymanagement

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class OtpActivity : AppCompatActivity() {

    private val TAG = "OTP_DEBUG"

    private lateinit var tvOtpMessage: TextView
    private lateinit var etOtp: TextInputEditText
    private lateinit var btnVerifyOtp: MaterialButton
    
    // Fake OTP implementation preserved for demo
    private var generatedOtp: String? = null
    
    // Firebase implementation
    private var phoneNumber: String? = null
    private var verificationId: String? = null
    private var isFirebaseFlow: Boolean = false
    private var is2faFlow: Boolean = false
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager
    private var flowId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var resendAt = 0L
    private var countdown: CountDownTimer? = null
    private var sending = false
    private var verifying = false
    private var completed = false
    private var requestInProgress = false

    private lateinit var smsBroadcastReceiver: SmsBroadcastReceiver
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        AuthUi.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        
        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        tvOtpMessage = findViewById(R.id.tvOtpMessage)
        etOtp = findViewById(R.id.etOtp)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)

        phoneNumber = intent.getStringExtra("phone")
        generatedOtp = intent.getStringExtra("otp")
        verificationId = savedInstanceState?.getString("verificationId") ?: intent.getStringExtra("verificationId")
        isFirebaseFlow = intent.getBooleanExtra("isFirebaseFlow", false)
        is2faFlow = intent.getBooleanExtra("is2faFlow", false)

        flowId = intent.getStringExtra("authFlowId")
        resendToken = savedInstanceState?.getParcelable("resendToken")
        resendAt = savedInstanceState?.getLong("resendAt") ?: 0L
        requestInProgress = savedInstanceState?.getBoolean("requestInProgress") ?: false
        if (isFirebaseFlow) {
            val pending = AuthFlows.store.get(flowId)
            if (pending == null || pending.verified) {
                AuthUi.message(this, getString(R.string.auth_session_expired))
                btnVerifyOtp.isEnabled = false
            } else {
                phoneNumber = pending.phone
                is2faFlow = pending.purpose == AuthPurpose.LOGIN
            }
        }
        tvOtpMessage.text = if (isFirebaseFlow) getString(R.string.auth_otp_destination, phoneNumber)
            else getString(R.string.otp_sent_to, phoneNumber)
        findViewById<TextView>(R.id.otpPurpose).setText(
            if (is2faFlow) R.string.auth_2fa_explanation else if (isFirebaseFlow)
                R.string.auth_reset_explanation else R.string.auth_demo_explanation)
        findViewById<View>(R.id.authBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnResendOtp).apply {
            visibility = if (isFirebaseFlow) View.VISIBLE else View.GONE
            setOnClickListener {
                if (!sending && !verifying && SystemClock.elapsedRealtime() >= resendAt) sendCode(true)
            }
        }
        etOtp.imeOptions = EditorInfo.IME_ACTION_DONE
        etOtp.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { btnVerifyOtp.performClick(); true } else false
        }
        if (isFirebaseFlow) {
            updateControls()
            startCountdown()
        }

        setupSmsUserConsent()

        btnVerifyOtp.setOnClickListener {
            if (sending || verifying || completed) return@setOnClickListener
            val enteredOtp = etOtp.text.toString().trim()
            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_otp), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isFirebaseFlow) {
                verifyFirebaseOtp(enteredOtp)
            } else {
                // Learning Module Flow (Fake OTP)
                if (enteredOtp == generatedOtp) {
                    Toast.makeText(this, getString(R.string.otp_verified), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyFirebaseOtp(code: String) {
        val id = verificationId
        if (id.isNullOrBlank() || AuthFlows.store.get(flowId) == null) {
            AuthUi.message(this, getString(R.string.auth_session_expired))
            return
        }
        if (!code.matches(Regex("[0-9]{6}"))) {
            etOtp.error = getString(R.string.auth_code_error)
            return
        }
        verifyCredential(PhoneAuthProvider.getCredential(id, code))
    }

    private fun verifyCredential(credential: PhoneAuthCredential) {
        if (completed || verifying || AuthFlows.store.get(flowId) == null) return
        verifying = true
        sending = false
        AuthUi.message(this, null)
        updateControls()
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            verifying = false
            if (task.isSuccessful) {
                val pending = AuthFlows.store.verified(flowId, task.result?.user?.phoneNumber)
                if (pending == null) {
                    AuthUi.message(this, getString(R.string.auth_session_expired))
                } else {
                    completed = true
                    requestInProgress = false
                    if (pending.purpose == AuthPurpose.LOGIN) {
                        val approved = AuthFlows.store.consume(flowId, AuthPurpose.LOGIN)
                        if (approved != null) {
                            sessionManager.trustDevice(approved.phone)
                            sessionManager.completeLogin(approved.phone, approved.fullName)
                            Toast.makeText(this, R.string.auth_verified_success, Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                    } else {
                        startActivity(Intent(this, ResetPasswordActivity::class.java)
                            .putExtra("authFlowId", pending.id))
                        finish()
                    }
                }
                auth.signOut()
            } else {
                AuthUi.message(this, AuthUi.firebaseError(task.exception))
            }
            updateControls()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            verifyCredential(credential)
        }

        override fun onVerificationFailed(error: FirebaseException) {
            if (completed) return
            sending = false
            requestInProgress = false
            AuthUi.message(this@OtpActivity, AuthUi.firebaseError(error))
            updateControls()
            startCountdown()
        }

        override fun onCodeAutoRetrievalTimeOut(id: String) {
            if (completed) return
            verificationId = id
            sending = false
            requestInProgress = false
            updateControls()
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            if (completed) return
            verificationId = id
            resendToken = token
            sending = false
            tvOtpMessage.text = getString(R.string.auth_otp_sent, phoneNumber)
            updateControls()
            startCountdown()
        }
    }

    private fun sendCode(forceResend: Boolean) {
        val pending = AuthFlows.store.get(flowId)
        if (pending == null || pending.verified) {
            AuthUi.message(this, getString(R.string.auth_session_expired))
            updateControls()
            return
        }
        if (completed || verifying) return
        if (forceResend && SystemClock.elapsedRealtime() < resendAt) return
        if (forceResend || !requestInProgress) {
            resendAt = SystemClock.elapsedRealtime() + 60_000
            verificationId = null
            etOtp.text?.clear()
        }
        sending = verificationId == null
        requestInProgress = true
        AuthUi.message(this, null)
        updateControls()
        startCountdown()
        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(pending.phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
        if (forceResend) resendToken?.let { builder.setForceResendingToken(it) }
        try {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (error: Exception) {
            sending = false
            requestInProgress = false
            AuthUi.message(this, AuthUi.firebaseError(error))
            updateControls()
        }
    }

    private fun updateControls() {
        val valid = AuthFlows.store.get(flowId)?.verified == false
        findViewById<View>(R.id.authProgress).visibility =
            if (sending || verifying) View.VISIBLE else View.GONE
        btnVerifyOtp.isEnabled = valid && !sending && !verifying && !completed && verificationId != null
        etOtp.isEnabled = !verifying && !completed
        btnVerifyOtp.setText(if (verifying) R.string.auth_verifying else if (sending)
            R.string.auth_sending else R.string.verify_otp)
        findViewById<View>(R.id.btnResendOtp).isEnabled =
            valid && !sending && !verifying && !completed && SystemClock.elapsedRealtime() >= resendAt
    }

    private fun startCountdown() {
        countdown?.cancel()
        val label = findViewById<TextView>(R.id.btnResendOtp)
        val remaining = (resendAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)
        if (remaining == 0L) {
            label.setText(R.string.auth_resend)
            updateControls()
            return
        }
        label.text = getString(R.string.auth_resend_countdown, (remaining + 999) / 1000)
        countdown = object : CountDownTimer(remaining, 1000) {
            override fun onTick(left: Long) {
                label.text = getString(R.string.auth_resend_countdown, (left + 999) / 1000)
            }
            override fun onFinish() {
                label.setText(R.string.auth_resend)
                updateControls()
            }
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("verificationId", verificationId)
        outState.putParcelable("resendToken", resendToken)
        outState.putLong("resendAt", resendAt)
        outState.putBoolean("requestInProgress", requestInProgress)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        countdown?.cancel()
        if (isFinishing && !completed) AuthFlows.store.cancel(flowId)
        super.onDestroy()
    }

    private fun navigateToMain() {
        sessionManager.login()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setupSmsUserConsent() {
        intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                message?.let { extractOtp(it) }
            }
        }

        SmsRetriever.getClient(this).startSmsUserConsent(null)
            .addOnSuccessListener {
                Log.d(TAG, "OtpActivity: startSmsUserConsent successfully started")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OtpActivity: startSmsUserConsent failed", e)
            }

        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver.setListener(object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
            override fun onSuccess(intent: Intent?) {
                intent?.let { intentLauncher.launch(it) }
            }

            override fun onFailure() {
                Log.d(TAG, "OtpActivity: Broadcast FAILURE/TIMEOUT")
            }
        })
    }

    private fun extractOtp(message: String) {
        val pattern = Pattern.compile("(\\d{6})")
        val matcher = pattern.matcher(message)
        if (matcher.find()) {
            val otp = matcher.group(0)
            etOtp.setText(otp)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isFirebaseFlow && !completed && AuthFlows.store.get(flowId)?.verified == false) {
            // Reentrant Firebase call reattaches callbacks after rotation or reCAPTCHA.
            if (requestInProgress || (verificationId == null && resendAt == 0L)) sendCode(false)
            startCountdown()
        }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsBroadcastReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null, RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsBroadcastReceiver, intentFilter, SmsRetriever.SEND_PERMISSION, null)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }
}
