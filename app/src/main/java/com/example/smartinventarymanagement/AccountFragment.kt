package com.example.smartinventarymanagement

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Locale

class AccountFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imgProfile: ImageView

    // IMAGE PICKER
    private val galleryLauncher =

        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                val destinationUri =
                    Uri.fromFile(
                        File(
                            requireContext().cacheDir,
                            "croppedImage.jpg"
                        )
                    )

                UCrop.of(it, destinationUri)

                    .withAspectRatio(1f, 1f)

                    .withMaxResultSize(500, 500)

                    .start(requireContext(), this)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_account,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences(
                "loginPrefs",
                Context.MODE_PRIVATE
            )

        val tvUsername =
            view.findViewById<TextView>(R.id.tvUsername)

        val btnLogout =
            view.findViewById<Button>(R.id.btnLogout)

        val switchDarkMode =
            view.findViewById<Switch>(R.id.switchDarkMode)

        val btnLanguage =
            view.findViewById<LinearLayout>(R.id.btnLanguage)

        val tvLanguageDisplay =
            view.findViewById<TextView>(R.id.tvLanguage)

        imgProfile =
            view.findViewById(R.id.imgProfile)

        // USERNAME
        val username =
            sharedPreferences.getString(
                "savedUsername",
                "User"
            )

        tvUsername.text = username

        // LANGUAGE DISPLAY
        val currentLang = sharedPreferences.getString("language", "en")
        tvLanguageDisplay.text = if (currentLang == "kn") getString(R.string.kannada) else getString(R.string.english)

        // LOAD PROFILE IMAGE
        val savedImage =
            sharedPreferences.getString(
                "profileImage",
                null
            )

        if (!savedImage.isNullOrEmpty()) {

            Glide.with(requireContext())
                .load(savedImage)
                .circleCrop()
                .into(imgProfile)
        }

        // PROFILE IMAGE CLICK
        imgProfile.setOnClickListener {

            val options = arrayOf(

                getString(R.string.view_photo),

                getString(R.string.change_photo),

                getString(R.string.remove_photo)
            )

            AlertDialog.Builder(requireContext())

                .setTitle(getString(R.string.profile_photo))

                .setItems(options) { _, which ->

                    when (which) {

                        // VIEW PHOTO
                        0 -> {

                            val intent = Intent(
                                requireContext(),
                                ProfilePhotoActivity::class.java
                            )

                            startActivity(intent)
                        }

                        // CHANGE PHOTO
                        1 -> {

                            checkPermissionAndOpenGallery()
                        }

                        // REMOVE PHOTO
                        2 -> {

                            sharedPreferences.edit()
                                .remove("profileImage")
                                .apply()

                            imgProfile.setImageResource(
                                R.drawable.profile_placeholder
                            )

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.profile_photo_removed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                .show()
        }

        // DARK MODE

        val isDarkMode =
            sharedPreferences.getBoolean(
                "darkMode",
                true
            )

        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->

            sharedPreferences.edit()
                .putBoolean("darkMode", isChecked)
                .apply()

            AppCompatDelegate.setDefaultNightMode(

                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // LOGOUT
        btnLogout.setOnClickListener {

            sharedPreferences.edit()
                .putBoolean("isLoggedIn", false)
                .apply()

            val intent = Intent(
                requireContext(),
                LoginActivity::class.java
            )

            startActivity(intent)

            requireActivity().finish()
        }

        // LANGUAGE
        btnLanguage.setOnClickListener {

            val languages = arrayOf(
                getString(R.string.english),
                getString(R.string.kannada)
            )

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_language)
                .setItems(languages) { _, which ->

                    when (which) {

                        0 -> setLocale("en")

                        1 -> setLocale("kn")
                    }
                }
                .show()
        }
    }

    // CHECK PERMISSION
    private fun checkPermissionAndOpenGallery() {

        val permission = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {

            Manifest.permission.READ_MEDIA_IMAGES

        } else {

            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            galleryLauncher.launch("image/*")

        } else {

            requestPermissions(
                arrayOf(permission),
                1001
            )
        }
    }

    // PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            galleryLauncher.launch("image/*")

        } else {

            Toast.makeText(
                requireContext(),
                getString(R.string.permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // CROP RESULT
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == UCrop.REQUEST_CROP &&
            resultCode == Activity.RESULT_OK
        ) {

            val resultUri =
                data?.let { UCrop.getOutput(it) }

            if (resultUri != null) {

                sharedPreferences.edit()

                    .putString(
                        "profileImage",
                        resultUri.toString()
                    )

                    .apply()

                Glide.with(requireContext())
                    .load(resultUri)
                    .circleCrop()
                    .into(imgProfile)

            } else {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.image_crop_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // UCROP ERROR
        else if (
            requestCode == UCrop.REQUEST_CROP
        ) {

            val cropError =
                data?.let { UCrop.getError(it) }

            Toast.makeText(
                requireContext(),
                cropError?.message ?: getString(R.string.crop_cancelled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setLocale(languageCode: String) {

        sharedPreferences.edit()
            .putString("language", languageCode)
            .apply()

        val locale = Locale(languageCode)

        Locale.setDefault(locale)

        val config = Configuration()

        config.setLocale(locale)

        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )

        requireActivity().recreate()
    }
}