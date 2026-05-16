package com.imagetopdf.ui.preview

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imagetopdf.R
import com.imagetopdf.data.model.ImageItem
import com.imagetopdf.data.model.Orientation
import com.imagetopdf.data.model.PDFDocument
import com.imagetopdf.data.model.PageSize
import com.imagetopdf.data.repository.PDFRepository
import com.imagetopdf.databinding.ActivityImagePreviewBinding
import com.imagetopdf.databinding.DialogPasswordInputBinding
import com.imagetopdf.databinding.DialogPdfPreviewBinding
import com.imagetopdf.ui.adapters.ImagePreviewAdapter
import com.imagetopdf.ui.custom.ImageGridAnimator
import com.imagetopdf.ui.custom.SmoothDragCallback
import com.imagetopdf.utils.AnimationUtils.animatePress
import com.imagetopdf.utils.AnimationUtils.slideUp
import com.imagetopdf.utils.FileUtils
import com.imagetopdf.utils.HapticUtils
import com.imagetopdf.utils.RecyclerViewOptimizer
import java.io.File

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private lateinit var adapter: ImagePreviewAdapter
    private lateinit var progressDialog: ProgressDialog
    private var smartNamingEnabled = true
    private var passwordProtectionEnabled = false
    private var cameraUri: android.net.Uri? = null
    
    private val viewModel: ImagePreviewViewModel by viewModels {
        ImagePreviewViewModelFactory(PDFRepository(this))
    }

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_SMART_NAMING = "smart_file_naming"
        private const val KEY_PASSWORD_PROTECTION = "enable_password_protection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()
        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupFileNameInput()
        setupClickListeners()
        observeViewModel()
        loadSelectedImages()
        setupProgressDialog()
    }

    private val pickMedia = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            addImages(uris)
        }
    }

    private val cameraLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null) {
            val uris = ArrayList<android.net.Uri>()
            uris.add(cameraUri!!)
            addImages(uris)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImages(uris: List<android.net.Uri>) {
        val newImages = uris.map { uri ->
            val name = FileUtils.getFileName(this, uri) ?: "Image_${System.currentTimeMillis()}"
            ImageItem(uri, name)
        }
        viewModel.addImages(newImages)
        Toast.makeText(this, "${newImages.size} images added", Toast.LENGTH_SHORT).show()
    }


    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        smartNamingEnabled = prefs.getBoolean(KEY_SMART_NAMING, true)
        passwordProtectionEnabled = prefs.getBoolean(KEY_PASSWORD_PROTECTION, false)
        
        // Show/hide password protection checkbox based on setting
        binding.checkboxPasswordProtection.visibility = if (passwordProtectionEnabled) View.VISIBLE else View.GONE
    }

    private fun setupFileNameInput() {
        if (smartNamingEnabled) {
            // Pre-populate with smart file name
            binding.etFileName.setText(FileUtils.generateSmartFileName())
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ImagePreviewAdapter { imageItem, position ->
            viewModel.removeImage(position)
        }
        binding.rvSelectedImages.adapter = adapter
        
        // Add smooth item animations
        binding.rvSelectedImages.itemAnimator = ImageGridAnimator()

        // Enhanced drag and drop with smooth animations
        val itemTouchHelper = ItemTouchHelper(object : SmoothDragCallback() {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
                    adapter.moveItem(fromPosition, toPosition)
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvSelectedImages)
        
        // Apply 120fps+ optimizations
        RecyclerViewOptimizer.optimize(binding.rvSelectedImages)
    }

    private fun setupSpinners() {
        // Page Size Spinner
        val pageSizes = PageSize.values().map { it.getDisplayName() }
        val pageSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageSizes)
        pageSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPageSize.adapter = pageSizeAdapter

        // Orientation Spinner
        val orientations = Orientation.values().map { it.getDisplayName() }
        val orientationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, orientations)
        orientationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrientation.adapter = orientationAdapter
    }

    private fun setupClickListeners() {
        binding.fabAddImages.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                showImageSourceDialog()
            }
        }

        binding.btnCreatePDF.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                createPDF()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.images.observe(this) { images ->
            adapter.submitList(images)
        }

        viewModel.isCreating.observe(this) { isCreating ->
            if (isCreating) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }
        }

        viewModel.creationResult.observe(this) { result ->
            result.fold(
                onSuccess = { pdfDocument ->
                    showPDFPreviewDialog(pdfDocument)
                },
                onFailure = { exception ->
                    Toast.makeText(
                        this,
                        exception.message ?: getString(R.string.msg_pdf_creation_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun loadSelectedImages() {
        val selectedImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("selected_images", ImageItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("selected_images")
        }

        if (selectedImages != null) {
            viewModel.setImages(selectedImages)
        } else {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getCompressionLevel(): com.imagetopdf.utils.PDFGenerator.CompressionLevel {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val quality = prefs.getString("pdf_quality", "Original") ?: "Original"
        return when (quality) {
            "Original" -> com.imagetopdf.utils.PDFGenerator.CompressionLevel.Original
            "High" -> com.imagetopdf.utils.PDFGenerator.CompressionLevel.High
            "Medium" -> com.imagetopdf.utils.PDFGenerator.CompressionLevel.Medium
            "Low" -> com.imagetopdf.utils.PDFGenerator.CompressionLevel.Low
            else -> com.imagetopdf.utils.PDFGenerator.CompressionLevel.Original
        }
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.msg_creating_pdf))
            setCancelable(false)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Image From")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = File(externalCacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
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

    private fun createPDF() {
        val fileName = binding.etFileName.text.toString().trim()
        if (fileName.isEmpty()) {
            binding.etFileName.error = getString(R.string.error_empty_file_name)
            return
        }

        val pageSize = PageSize.values()[binding.spinnerPageSize.selectedItemPosition]
        val orientation = Orientation.values()[binding.spinnerOrientation.selectedItemPosition]
        val compressionLevel = getCompressionLevel()

        // Update image order from adapter before creating PDF
        viewModel.updateImageOrder(adapter.getItems())

        // Check if password protection is enabled and checked
        if (passwordProtectionEnabled && binding.checkboxPasswordProtection.isChecked) {
            showPasswordDialog(fileName, pageSize, orientation, compressionLevel)
        } else {
            viewModel.createPDF(fileName, pageSize, orientation, compressionLevel)
        }
    }

    private fun showPasswordDialog(
        fileName: String, 
        pageSize: PageSize, 
        orientation: Orientation,
        compressionLevel: com.imagetopdf.utils.PDFGenerator.CompressionLevel
    ) {
        val dialogBinding = DialogPasswordInputBinding.inflate(LayoutInflater.from(this))
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Animate bottom sheet slide-up
        dialogBinding.root.post {
            dialogBinding.root.slideUp(300)
        }
        
        dialogBinding.btnConfirm.setOnClickListener {
            val password = dialogBinding.etPassword.text.toString()
            val confirmPassword = dialogBinding.etConfirmPassword.text.toString()

            // Validate password
            when {
                password.isEmpty() -> {
                    dialogBinding.tilPassword.error = getString(R.string.error_empty_password) // Corrected error string
                }
                password.length < 6 -> {
                    dialogBinding.tilPassword.error = getString(R.string.error_password_too_short)
                }
                password != confirmPassword -> {
                    dialogBinding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
                }
                else -> {
                    // Password is valid, create encrypted PDF
                    bottomSheetDialog.dismiss()
                    viewModel.createEncryptedPDF(fileName, pageSize, orientation, password, compressionLevel)
                }
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showPDFPreviewDialog(pdfDocument: PDFDocument) {
        val dialogBinding = DialogPdfPreviewBinding.inflate(LayoutInflater.from(this))
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Set PDF info
        dialogBinding.tvFileName.text = "${pdfDocument.name}.pdf"
        dialogBinding.tvFileLocation.text = FileUtils.getDefaultPDFDirectory().absolutePath
        dialogBinding.tvFileSize.text = pdfDocument.getFormattedSize()

        // Set preview image (use PDF icon as placeholder)
        dialogBinding.ivPdfPreview.setImageResource(R.drawable.ic_pdf)

        // Handle Open PDF button
        dialogBinding.btnOpenPdf.setOnClickListener {
            val repository = PDFRepository(this)
            val intent = repository.getOpenIntent(pdfDocument)
            if (intent != null) {
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show()
                }
            }
            bottomSheetDialog.dismiss()
            finish()
        }

        // Handle Share button
        dialogBinding.btnShare.setOnClickListener {
            val repository = PDFRepository(this)
            val intent = repository.getShareIntent(pdfDocument)
            if (intent != null) {
                startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
            }
            bottomSheetDialog.dismiss()
            finish()
        }

        // Handle Done button
        dialogBinding.btnDone.setOnClickListener {
            bottomSheetDialog.dismiss()
            finish()
        }

        bottomSheetDialog.show()
    }
}
