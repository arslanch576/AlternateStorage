package com.coderobust.alternatestorage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.coderobust.alternatestorage.CloudinaryUploadHelper.Companion.initializeCloudinary

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeCloudinary(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            chooseImageFromGallery()
        }

    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                uploadImage(uri)
            } else {
                Log.e("Gallery", "No image selected")
            }
        }
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun uploadImage(uri: Uri) {
        // Convert the URI to an actual file path
        val filePath = getRealPathFromURI(uri)
        if (filePath != null) {
            CloudinaryUploadHelper(this).uploadFile(filePath) { success, result ->
                if (success) {
                    Log.d("Cloudinary", "File uploaded successfully. URL: $result")
                    // Optionally, display the uploaded image
                    Glide.with(this)
                        .load(result)
                        .into(findViewById(R.id.imageView2))
                } else {
                    Log.e("Cloudinary", "Upload failed: $result")
                }
            }
        } else {
            Log.e("Gallery", "Failed to get file path from URI")
        }
    }

    // Helper function to get the real file path from URI
    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }


}