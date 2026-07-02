package com.example.smartinventarymanagement

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.chrisbanes.photoview.PhotoView

class ProfilePhotoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_photo)

        val photoView = findViewById<PhotoView>(R.id.fullProfileImage)

        val sharedPref = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val imageUriString = sharedPref.getString("profileImage", null)

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            Glide.with(this)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Force reload
                .skipMemoryCache(true)
                .into(photoView)
        }
    }
}
