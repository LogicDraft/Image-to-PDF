package com.imagetopdf.ui.selection

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.imagetopdf.R
import com.imagetopdf.databinding.ActivityImageSelectionBinding
import com.imagetopdf.ui.adapters.ImageGridAdapter
import com.imagetopdf.ui.custom.ImageGridAnimator
import com.imagetopdf.ui.preview.ImagePreviewActivity
import com.imagetopdf.utils.AnimationUtils.animatePress
import com.imagetopdf.utils.HapticUtils
import com.imagetopdf.utils.PermissionManager
import com.imagetopdf.utils.RecyclerViewOptimizer
import java.io.File

class ImageSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageSelectionBinding
    private val viewModel: ImageSelectionViewModel by viewModels()
    private lateinit var adapter: ImageGridAdapter
    private var capturedImageUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            viewModel.loadImages(this)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.loadImages(this)
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            updateEmptyState(true)
        }
    }

    private val systemPicker = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addSystemImages(this, uris)
            Toast.makeText(this, "${uris.size} images added", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        checkPermissionsAndLoadImages()
    }

    override fun onResume() {
        super.onResume()
        if (
            PermissionManager.hasStoragePermission(this) &&
            viewModel.images.value.isNullOrEmpty() &&
            viewModel.isLoading.value != true
        ) {
            viewModel.loadImages(this)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ImageGridAdapter { imageItem, position ->
            viewModel.toggleImageSelection(imageItem, position)
        }
        binding.rvImages.adapter = adapter
        
        // Add smooth item animations
        binding.rvImages.itemAnimator = ImageGridAnimator()
        
        // Apply 120fps+ optimizations
        RecyclerViewOptimizer.optimize(binding.rvImages)
    }

    private fun setupClickListeners() {
        binding.btnSystemGallery.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                systemPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        binding.btnCamera.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                if (PermissionManager.hasCameraPermission(this)) {
                    launchCamera()
                } else {
                    PermissionManager.requestCameraPermission(this)
                }
            }
        }

        binding.btnSelectAll.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                if (viewModel.getSelectedCount() == adapter.itemCount) {
                    viewModel.deselectAll()
                } else {
                    viewModel.selectAll()
                }
            }
        }

        binding.fabNext.setOnClickListener { view ->
            val selectedImages = viewModel.selectedImages.value
            if (selectedImages.isNullOrEmpty()) {
                HapticUtils.performError(view)
                Toast.makeText(this, R.string.msg_select_images, Toast.LENGTH_SHORT).show()
            } else {
                view.animatePress {
                    HapticUtils.performSuccess(view)
                    val intent = Intent(this, ImagePreviewActivity::class.java).apply {
                        putParcelableArrayListExtra("selected_images", ArrayList(selectedImages))
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.images.observe(this) { images ->
            adapter.submitList(images)
            updateEmptyState(images.isEmpty())
        }

        viewModel.selectedImages.observe(this) { selected ->
            binding.toolbar.title = if (selected.isEmpty()) {
                getString(R.string.title_all_images)
            } else {
                getString(R.string.images_selected, selected.size)
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.tvLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvImages.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        
        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                updateEmptyState(true)
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty && viewModel.isLoading.value != true) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun checkPermissionsAndLoadImages() {
        if (PermissionManager.hasStoragePermission(this)) {
            viewModel.loadImages(this)
        } else {
            permissionLauncher.launch(PermissionManager.getStoragePermissions())
        }
    }

    private fun launchCamera() {
        val photoFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        capturedImageUri = uri
        cameraLauncher.launch(uri)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionManager.STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    viewModel.loadImages(this)
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                    updateEmptyState(true)
                }
            }
            PermissionManager.CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera()
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
