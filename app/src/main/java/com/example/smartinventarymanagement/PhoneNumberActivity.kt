package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
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
            val phone = etPhoneNumber.text.toString().trim()
            if (validatePhone(phone)) {
                val otp = generateOtp()

                // Temporary Debug Mode: Show OTP to user for testing
                AlertDialog.Builder(this)
                    .setTitle("Developer Testing Mode")
                    .setMessage("Generated OTP:\n$otp")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("phone", phone)
                        intent.putExtra("otp", otp)
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
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    /**
     * Generates a random 6-digit OTP for Phase-1 testing.
     * Future-ready: Will be replaced by real SMS service/API call.
     */
    private fun generateOtp(): String {
        val otp = Random.nextInt(100000, 999999)
        return otp.toString()
    }
}