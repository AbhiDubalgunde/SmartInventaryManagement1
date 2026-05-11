package com.example.smartinventarymanagement

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.room.Room
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AddItemActivity : AppCompatActivity() {

    private lateinit var itemDao: ItemDao

    private var itemId = 0

    private var isEditMode = false

    // ✅ STRING TYPE
    private var selectedImageUri: String? = null

    companion object {

        const val IMAGE_PICK_CODE = 1001

        const val PERMISSION_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_item)

        val db = DatabaseProvider.getDatabase(this)

        itemDao = db.itemDao()

        val etName =
            findViewById<EditText>(R.id.etName)

        val etQty =
            findViewById<EditText>(R.id.etQuantity)

        val etPrice =
            findViewById<EditText>(R.id.etPrice)

        val btnSave =
            findViewById<Button>(R.id.btnSave)

        val spinner =
            findViewById<Spinner>(R.id.spCategory)

        val btnSelectImage =
            findViewById<Button>(R.id.btnSelectImage)

        val imgPreview =
            findViewById<ImageView>(R.id.imgPreview)

        val categories = listOf(
            "Grocery",
            "Electronics",
            "Personal Care",
            "Stationery"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinner.adapter = adapter

        val sharedPref =
            getSharedPreferences("MyPrefs", MODE_PRIVATE)

        val lastCategory =
            sharedPref.getString("last_category", null)

        if (lastCategory != null) {

            val position =
                categories.indexOf(lastCategory)

            if (position >= 0) {

                spinner.setSelection(position)
            }
        }

        // IMAGE PICKER

        btnSelectImage.setOnClickListener {

            val permission = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {

                Manifest.permission.READ_MEDIA_IMAGES

            } else {

                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                openGallery()

            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    PERMISSION_CODE
                )
            }
        }        // EDIT MODE

        if (intent.hasExtra("id")) {

            isEditMode = true

            itemId =
                intent.getIntExtra("id", 0)

            val name =
                intent.getStringExtra("name")

            val qty =
                intent.getIntExtra("qty", 0)

            val price =
                intent.getDoubleExtra("price", 0.0)

            val category =
                intent.getStringExtra("category")

            val imageUri =
                intent.getStringExtra("imageUri")

            etName.setText(name)

            etQty.setText(qty.toString())

            etPrice.setText(price.toString())

            val position =
                categories.indexOf(category)

            if (position >= 0) {

                spinner.setSelection(position)
            }

            // ✅ SHOW OLD IMAGE
            if (!imageUri.isNullOrEmpty()) {
                selectedImageUri = imageUri
                Glide.with(this)
                    .load(Uri.parse(imageUri))
                    .into(imgPreview)
            }

            btnSave.text = getString(R.string.update_item)
        }

        // SAVE

        btnSave.setOnClickListener {

            val name =
                etName.text.toString()

            val qty =
                etQty.text.toString()
                    .toIntOrNull() ?: 0

            val price =
                etPrice.text.toString()
                    .toDoubleOrNull() ?: 0.0

            val category =
                spinner.selectedItem.toString()

            val isLowStock =
                qty < 5

            // ✅ NOW SAVES STRING URI

            val item = Item(
                id = itemId,
                name = name,
                quantity = qty,
                price = price,
                category = category,
                isLowStock = isLowStock,
                imageUri = selectedImageUri
            )

            lifecycleScope.launch {

                if (isEditMode) {

                    itemDao.update(item)

                    Toast.makeText(
                        this@AddItemActivity,
                        getString(R.string.item_updated),
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    itemDao.insert(item)

                    Toast.makeText(
                        this@AddItemActivity,
                        getString(R.string.item_added),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                sharedPref.edit()
                    .putString(
                        "last_category",
                        category
                    )
                    .apply()

                finish()
            }
        }
    }

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
            requestCode == IMAGE_PICK_CODE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {
            val imgPreview = findViewById<ImageView>(R.id.imgPreview)
            val uri = data.data
            if (uri != null) {
                val internalUri = saveImageToInternalStorage(uri)
                if (internalUri != null) {
                    selectedImageUri = internalUri.toString()
                    Glide.with(this).load(internalUri).into(imgPreview)
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileName = "item_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openGallery() {

        val intent = Intent(
            Intent.ACTION_PICK
        )

        intent.type = "image/*"

        startActivityForResult(
            intent,
            IMAGE_PICK_CODE
        )
    }

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
            requestCode == PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            openGallery()

        } else {

            Toast.makeText(
                this,
                getString(R.string.gallery_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}