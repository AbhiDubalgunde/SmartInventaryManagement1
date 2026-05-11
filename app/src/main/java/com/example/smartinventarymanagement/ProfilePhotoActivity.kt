package com.example.smartinventarymanagement

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProfilePhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile_photo)

        val imageView =
            findViewById<ImageView>(R.id.fullProfileImage)

        val sharedPref =
            getSharedPreferences(
                "loginPrefs",
                Context.MODE_PRIVATE
            )

        val image =
            sharedPref.getString(
                "profileImage",
                null
            )

        if (!image.isNullOrEmpty()) {

            Glide.with(this)
                .load(image)
                .into(imageView)
        }
    }
}