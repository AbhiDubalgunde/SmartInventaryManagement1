package com.example.smartinventarymanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val etFullName = findViewById<EditText>(R.id.etRegisterFullName)
        val etPhone = findViewById<EditText>(R.id.etRegisterPhone)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!phone.startsWith("+")) {
                Toast.makeText(this, "Please include country code (e.g., +91)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(fullName, phone, password)
        }
    }

    private fun registerUser(fullName: String, phone: String, password: String) {
        val db = DatabaseProvider.getDatabase(this)
        lifecycleScope.launch {
            val existingUser = db.userDao().getUserByPhone(phone)
            if (existingUser != null) {
                Toast.makeText(this@RegisterActivity, getString(R.string.phone_already_registered), Toast.LENGTH_SHORT).show()
            } else {
                val user = User(
                    fullName = fullName,
                    phoneNumber = phone,
                    password = password
                )
                db.userDao().insert(user)
                Toast.makeText(this@RegisterActivity, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

}