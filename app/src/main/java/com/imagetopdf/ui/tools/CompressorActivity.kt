package com.imagetopdf.ui.tools

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.imagetopdf.R
import com.imagetopdf.databinding.ActivityCompressorBinding
import com.imagetopdf.ui.base.BaseActivity
import com.imagetopdf.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CompressorActivity : BaseActivity() {

    private lateinit var binding: ActivityCompressorBinding
    private var selectedImageUri: Uri? = null
    private var compressedFile: File? = null
    private var originalSize: Long = 0

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleImageSelection(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.cardSelectImage.setOnClickListener {
            imageLauncher.launch("image/*")
        }

        binding.btnCompress.setOnClickListener {
            compressImage()
        }
        
        binding.btnShare.setOnClickListener {
            shareCompressedImage()
        }
        
        binding.btnSave.setOnClickListener {
            saveCompressedImage()
        }
    }

    private fun handleImageSelection(uri: Uri) {
        selectedImageUri = uri
        binding.ivPreview.setImageURI(uri)
        binding.tvSelectImage.text = "Change Image"
        
        try {
            contentResolver.openFileDescriptor(uri, "r")?.use {
                originalSize = it.statSize
                binding.tvOriginalSize.text = "Original Size: ${FileUtils.formatFileSize(originalSize)}"
            }
        } catch (e: Exception) {
            binding.tvOriginalSize.text = "Size: Unknown"
        }
        
        binding.btnCompress.isEnabled = true
        binding.layoutResult.visibility = View.GONE
    }

    private fun compressImage() {
        val targetSizeText = binding.etTargetSize.text.toString()
        val targetSizeKB = targetSizeText.toLongOrNull() ?: 500
        val targetSizeBytes = targetSizeKB * 1024
        
        if (selectedImageUri == null) return
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.btnCompress.isEnabled = false
                    binding.btnCompress.text = "Compressing..."
                }

                val result = performCompression(selectedImageUri!!, targetSizeBytes)
                
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        displayResult(result)
                    } else {
                        Toast.makeText(this@CompressorActivity, "Failed to compress", Toast.LENGTH_SHORT).show()
                    }
                    binding.btnCompress.isEnabled = true
                    binding.btnCompress.text = "Compress Image"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CompressorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnCompress.isEnabled = true
                    binding.btnCompress.text = "Compress Image"
                }
            }
        }
    }

    private suspend fun performCompression(uri: Uri, targetBytes: Long): Pair<File, Int>? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        var bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        
        if (bitmap == null) return null
        
        var stream = ByteArrayOutputStream()
        
        // Initial check: if already smaller, just return original (or copy)
        // But since we want to ensure it's a JPG and maybe slightly compressed if requested
        // Let's start with high quality.
        
        var minQuality = 5
        var maxQuality = 100
        var bestQuality = 100
        var bestStream: ByteArrayOutputStream? = null
        
        // Binary search for best quality at original resolution
        while (minQuality <= maxQuality) {
            val midQuality = (minQuality + maxQuality) / 2
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, midQuality, stream)
            
            val size = stream.size()
            
            if (size <= targetBytes) {
                bestQuality = midQuality
                bestStream = stream // Keep this valid result
                minQuality = midQuality + 1 // Try for better quality
            } else {
                maxQuality = midQuality - 1 // Reduce quality
            }
        }
        
        // If we found a valid quality at original resolution
        if (bestStream != null) {
            return saveCompressedFile(bestStream!!, bestQuality)
        }
        
        // If even lowest quality at original resolution is too big, we must resize
        // Reset quality to a reasonable start for resizing
        var quality = 80
        var currentBitmap = bitmap
        
        while (true) {
            val matrix = Matrix()
            matrix.postScale(0.8f, 0.8f) // Reduce by 20%
            val scaledBitmap = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
            
            // Recycle old bitmap to save memory (unless it's the original immutable one, handle carefully)
            if (currentBitmap != bitmap) {
                currentBitmap.recycle()
            }
            currentBitmap = scaledBitmap
            
            if (currentBitmap.width < 100 || currentBitmap.height < 100) break // Stop if too small
            
            stream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            
            if (stream.size() <= targetBytes) {
                return saveCompressedFile(stream, quality)
            }
            
            // If still too big after resize, try reducing quality further for this size
            // (Simplified: just continue loop which resizes again)
             // Or could do a quick check with lower quality here?
            stream.reset()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            if (stream.size() <= targetBytes) {
                 return saveCompressedFile(stream, 50)
            }
        }
        
        return null // Failed to compress to target size
    }

    private fun saveCompressedFile(stream: ByteArrayOutputStream, quality: Int): Pair<File, Int> {
        val outputFile = File(externalCacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val ops = FileOutputStream(outputFile)
        ops.write(stream.toByteArray())
        ops.close()
        compressedFile = outputFile
        return Pair(outputFile, quality)
    }

    private fun displayResult(result: Pair<File, Int>) {
        val file = result.first
        val quality = result.second
        
        binding.layoutResult.visibility = View.VISIBLE
        binding.tvCompressedSize.text = "New Size: ${FileUtils.formatFileSize(file.length())}"
        binding.tvQualityUsed.text = "Final Quality: $quality%"
        
        Toast.makeText(this, "Compression Complete!", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareCompressedImage() {
        compressedFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Share Image"))
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to share", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveCompressedImage() {
        compressedFile?.let { file ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Save to Downloads/ImageToPDF/Compressed (or generic pictures)
                    val destDir = File(FileUtils.getDefaultPDFDirectory(), "Compressed")
                    if (!destDir.exists()) destDir.mkdirs()
                    
                    val fileName = "compressed_${System.currentTimeMillis()}.jpg"
                    val destFile = File(destDir, fileName)
                    
                    file.copyTo(destFile, overwrite = true)
                    
                    withContext(Dispatchers.Main) {
                         Toast.makeText(this@CompressorActivity, "Saved to ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                         Toast.makeText(this@CompressorActivity, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
