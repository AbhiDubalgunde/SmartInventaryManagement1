package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.random.Random

class PhoneNumberActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var btnGenerateOtp: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number)

        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnGenerateOtp = findViewById(R.id.btnGenerateOtp)

        btnGenerateOtp.setOnClickListener {
            // ===========================
            // DEBUG POINT 1
            // Generate button clicked
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 1 : Generate button clicked")
            val phone = etPhoneNumber.text.toString().trim()
            
            // PLACE BREAKPOINT HERE
            if (validatePhone(phone)) {
                val otp = generateOtp()

                // Temporary Debug Mode: Show OTP to user for testing
                AlertDialog.Builder(this)
                    .setTitle("Developer Testing Mode")
                    .setMessage("Generated OTP:\n$otp")
                    .setPositiveButton("OK") { _, _ ->
                        // ===========================
                        // DEBUG POINT 5
                        // Intent created
                        // ===========================
                        Log.d("OTP_DEBUG", "DEBUG POINT 5 : Intent created")
                        // PLACE BREAKPOINT HERE
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("phone", phone)
                        intent.putExtra("otp", otp)

                        // ===========================
                        // DEBUG POINT 6
                        // Opening OtpActivity
                        // ===========================
                        Log.d("OTP_DEBUG", "DEBUG POINT 6 : Opening OtpActivity")
                        // PLACE BREAKPOINT HERE
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .show()

            } else {
                Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Validates if the phone number is exactly 10 digits.
     * Future-ready: Can be updated to include regex or international formats.
     */
    private fun validatePhone(phone: String): Boolean {
        // ===========================
        // DEBUG POINT 2
        // Phone validation starts
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 2 : Phone validation starts")
        
        // PLACE BREAKPOINT HERE
        val isValid = phone.length == 10 && phone.all { it.isDigit() }
        
        if (isValid) {
            // ===========================
            // DEBUG POINT 3
            // Phone validation success
            // ===========================
            Log.d("OTP_DEBUG", "DEBUG POINT 3 : Phone validation success")
        }
        return isValid
    }

    /**
     * Generates a random 6-digit OTP for Phase-1 testing.
     * Future-ready: Will be replaced by real SMS service/API call.
     */
    private fun generateOtp(): String {
        // ===========================
        // DEBUG POINT 4
        // Random OTP generated
        // ===========================
        Log.d("OTP_DEBUG", "DEBUG POINT 4 : Random OTP generated")
        
        // PLACE BREAKPOINT HERE
        val otp = Random.nextInt(100000, 999999)
        return otp.toString()
    }
}