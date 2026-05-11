package com.example.smartinventarymanagement

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ LANGUAGE APPLY
        val pref =
            getSharedPreferences(
                "loginPrefs",
                MODE_PRIVATE
            )

        val language =
            pref.getString(
                "language",
                "en"
            )

        val locale =
            Locale(language!!)

        Locale.setDefault(locale)

        val config =
            Configuration()

        config.setLocale(locale)

        resources.updateConfiguration(
            config,
            resources.displayMetrics
        )

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(
            "loginPrefs",
            MODE_PRIVATE
        )

        val isLoggedIn = sharedPreferences.getBoolean(
            "isLoggedIn",
            false
        )

        if (isLoggedIn) {

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }

        val etUsername =
            findViewById<EditText>(R.id.etUsername)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val btnLogin =
            findViewById<Button>(R.id.btnLogin)

        val tvCreateAccount =
            findViewById<TextView>(R.id.tvCreateAccount)

        // OPEN REGISTER
        tvCreateAccount.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // LOGIN
        btnLogin.setOnClickListener {

            val username =
                etUsername.text.toString().trim()

            val password =
                etPassword.text.toString().trim()

            val savedUsername =
                sharedPreferences.getString(
                    "savedUsername",
                    ""
                )

            val savedPassword =
                sharedPreferences.getString(
                    "savedPassword",
                    ""
                )

            if (
                username == savedUsername &&
                password == savedPassword
            ) {

                sharedPreferences.edit()

                    .putBoolean(
                        "isLoggedIn",
                        true
                    )

                    .apply()

                Toast.makeText(
                    this,
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(this, MainActivity::class.java)
                )

                finish()

            } else {

                Toast.makeText(
                    this,
                    getString(R.string.invalid_login),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}