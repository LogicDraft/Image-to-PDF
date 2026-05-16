package com.imagetopdf.ui.files

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.imagetopdf.R
import com.imagetopdf.data.model.PDFDocument
import com.imagetopdf.data.repository.PDFRepository
import com.imagetopdf.databinding.ActivityPdfFilesBinding
import com.imagetopdf.ui.adapters.PDFFilesAdapter
import com.imagetopdf.ui.custom.ImageGridAnimator
import com.imagetopdf.ui.selection.ImageSelectionActivity
import com.imagetopdf.utils.AnimationUtils.animatePress
import com.imagetopdf.utils.HapticUtils
import com.imagetopdf.utils.PermissionManager
import com.imagetopdf.utils.RecyclerViewOptimizer

class PDFFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfFilesBinding
    private lateinit var adapter: PDFFilesAdapter
    private lateinit var repository: PDFRepository
    
    private val viewModel: PDFFilesViewModel by viewModels {
        PDFFilesViewModelFactory(PDFRepository(this))
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startImageSelection()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = PDFRepository(this)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        viewModel.loadPDFs()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPDFs()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PDFFilesAdapter(
            onOpenClick = { pdfDocument -> openPDF(pdfDocument) },
            onShareClick = { pdfDocument -> sharePDF(pdfDocument) },
            onDeleteClick = { pdfDocument -> showDeleteConfirmation(pdfDocument) }
        )
        binding.rvPDFFiles.adapter = adapter
        
        // Add smooth item animations
        binding.rvPDFFiles.itemAnimator = ImageGridAnimator()
        
        // Apply 120fps+ optimizations
        RecyclerViewOptimizer.optimize(binding.rvPDFFiles)
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                openImageSelectionWithPermission()
            }
        }
        
        // Handle empty state button click
        binding.layoutEmpty.btnCreatePdf.setOnClickListener { view ->
            view.animatePress {
                HapticUtils.performMediumClick(view)
                openImageSelectionWithPermission()
            }
        }
    }

    private fun openImageSelectionWithPermission() {
        if (PermissionManager.hasStoragePermission(this)) {
            startImageSelection()
        } else {
            permissionLauncher.launch(PermissionManager.getStoragePermissions())
        }
    }

    private fun startImageSelection() {
        startActivity(Intent(this, ImageSelectionActivity::class.java))
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private fun observeViewModel() {
        viewModel.pdfFiles.observe(this) { pdfs ->
            adapter.submitList(pdfs)
            binding.layoutEmpty.root.visibility = if (pdfs.isEmpty()) View.VISIBLE else View.GONE
            binding.rvPDFFiles.visibility = if (pdfs.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun openPDF(pdfDocument: PDFDocument) {
        val intent = repository.getOpenIntent(pdfDocument)
        if (intent != null) {
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, R.string.msg_no_app_to_open, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sharePDF(pdfDocument: PDFDocument) {
        val intent = repository.getShareIntent(pdfDocument)
        if (intent != null) {
            startActivity(Intent.createChooser(intent, "Share PDF"))
        }
    }

    private fun showDeleteConfirmation(pdfDocument: PDFDocument) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deletePDF(pdfDocument)
                Toast.makeText(this, R.string.msg_pdf_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
