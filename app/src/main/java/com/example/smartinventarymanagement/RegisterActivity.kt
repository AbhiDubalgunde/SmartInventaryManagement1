package com.example.smartinventarymanagement

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences(
            "loginPrefs",
            MODE_PRIVATE
        )

        val etUsername = findViewById<EditText>(R.id.etRegisterUsername)

        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.enter_all_fields),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                sharedPreferences.edit()
                    .putString("savedUsername", username)
                    .putString("savedPassword", password)
                    .apply()

                Toast.makeText(
                    this,
                    getString(R.string.registration_successful),
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )

                finish()
            }
        }
    }
}