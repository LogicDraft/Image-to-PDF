package com.imagetopdf.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.imagetopdf.ui.base.BaseActivity
import com.imagetopdf.databinding.ActivityMainBinding
import com.imagetopdf.ui.files.PDFFilesActivity
import com.imagetopdf.ui.selection.ImageSelectionActivity
import com.imagetopdf.ui.settings.SettingsActivity
import com.imagetopdf.utils.AnimationUtils.animatePress
import com.imagetopdf.utils.HapticUtils
import com.imagetopdf.utils.PermissionManager
import com.imagetopdf.R
import com.bumptech.glide.Glide

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            navigateToImageSelection()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private var cameraUri: android.net.Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            val uris = ArrayList<android.net.Uri>()
            uris.add(cameraUri!!)
            navigateToImagePreview(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateBackground()
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        updateBackground()
    }

    private fun updateBackground() {
        val prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val backgroundUri = prefs.getString("home_background_uri", null)
        
        if (backgroundUri != null) {
            try {
                // Use Glide for better image handling
                Glide.with(this)
                    .load(android.net.Uri.parse(backgroundUri))
                    .centerCrop()
                    .into(binding.ivBackground)
                
                binding.ivBackground.alpha = 0.5f // Increase opacity for custom images
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivBackground.setImageDrawable(null)
            }
        } else {
            // Default behavior: No background
            binding.ivBackground.setImageDrawable(null)
        }
    }

    private fun setupClickListeners() {
        binding.cardCreatePDF.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                checkPermissionAndNavigate()
            }
        }

        binding.cardCamera.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                checkCameraPermissionAndLaunch()
            }
        }

        binding.cardCompress.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                startActivity(Intent(this, com.imagetopdf.ui.tools.CompressorActivity::class.java))
                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
            }
        }

        binding.btnPDFFiles.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                startActivity(Intent(this, PDFFilesActivity::class.java))
                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
            }
        }

        binding.btnSettings.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                startActivity(Intent(this, SettingsActivity::class.java))
                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
            }
        }
    }
    
    private fun checkPermissionAndNavigate() {
        if (PermissionManager.hasStoragePermission(this)) {
            navigateToImageSelection()
        } else {
            permissionLauncher.launch(PermissionManager.getStoragePermissions())
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (PermissionManager.hasCameraPermission(this)) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(PermissionManager.getCameraPermission())
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = java.io.File(externalCacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            cameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to launch camera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleIncomingIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND) {
            (intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM))?.let { uri ->
                val uris = ArrayList<android.net.Uri>()
                uris.add(uri)
                navigateToImagePreview(uris)
            }
        } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            intent.getParcelableArrayListExtra<android.net.Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                navigateToImagePreview(uris)
            }
        }
    }

    private fun navigateToImageSelection() {
        startActivity(Intent(this, ImageSelectionActivity::class.java))
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private fun navigateToImagePreview(uris: ArrayList<android.net.Uri>) {
        val imageItems = ArrayList<com.imagetopdf.data.model.ImageItem>()
        uris.forEachIndexed { index, uri ->
            val item = getImageItemFromUri(uri)
            item.order = index
            item.isSelected = true
            imageItems.add(item)
        }
        
        val intent = Intent(this, com.imagetopdf.ui.preview.ImagePreviewActivity::class.java)
        intent.putParcelableArrayListExtra("selected_images", imageItems)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private fun getImageItemFromUri(uri: android.net.Uri): com.imagetopdf.data.model.ImageItem {
        var dn = "Shared Image"
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                dn = cursor.getString(nameIndex)
            }
            cursor.close()
        }
        return com.imagetopdf.data.model.ImageItem(uri, dn)
    }
}
