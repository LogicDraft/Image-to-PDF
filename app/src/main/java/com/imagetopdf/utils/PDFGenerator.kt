package com.imagetopdf.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.imagetopdf.data.model.ImageItem
import com.imagetopdf.data.model.Orientation
import com.imagetopdf.data.model.PageSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

class PDFGenerator(private val context: Context) {
    
    // Max dimension for high quality but optimized size
    private val MAX_IMAGE_DIMENSION = 2500

    enum class CompressionLevel {
        Original,
        High,
        Medium,
        Low
    }

    suspend fun createPDF(
        images: List<ImageItem>,
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        outputDir: File,
        compressionLevel: CompressionLevel
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            
            // Calculate base page dimensions based on orientation
            val basePageWidth = if (pageSize == PageSize.ORIGINAL) 0f else {
                 if (orientation == Orientation.PORTRAIT) pageSize.width else pageSize.height
            }
            val basePageHeight = if (pageSize == PageSize.ORIGINAL) 0f else {
                 if (orientation == Orientation.PORTRAIT) pageSize.height else pageSize.width
            }
            
            // Determine max dimension based on compression level
            val maxDimension = when (compressionLevel) {
                CompressionLevel.Original -> 2500
                CompressionLevel.High -> 2000
                CompressionLevel.Medium -> 1500
                CompressionLevel.Low -> 1000
            }

            images.sortedBy { it.order }.forEachIndexed { index, imageItem ->
                var bitmap: Bitmap? = null
                try {
                    // Load bitmap with optimization and rotation
                    bitmap = if (pageSize == PageSize.ORIGINAL) {
                        loadOptimizedBitmap(imageItem.uri, maxDimension, maxDimension)
                    } else {
                        // For fixed page sizes, we still want to limit the bitmap size to avoid OOM and reduce file size
                        // but it should be at least the page size if possible, or scaled down if the page is huge.
                        // However, A4 at 72dpi is roughly 595x842. standard screen usage might be higher.
                        // We will use the maxDimension as a cap.
                        val targetWidth = min(basePageWidth.toInt(), maxDimension)
                        val targetHeight = min(basePageHeight.toInt(), maxDimension)
                         loadOptimizedBitmap(imageItem.uri, targetWidth, targetHeight)
                    }
                    
                    if (bitmap == null) return@forEachIndexed
                
                    // Determine dimensions
                    val currentPageWidth = if (pageSize == PageSize.ORIGINAL) bitmap.width.toFloat() else basePageWidth
                    val currentPageHeight = if (pageSize == PageSize.ORIGINAL) bitmap.height.toFloat() else basePageHeight

                    // Create page
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        currentPageWidth.toInt(),
                        currentPageHeight.toInt(),
                        index + 1
                    ).create()
                
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                
                    if (pageSize == PageSize.ORIGINAL) {
                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
                    } else {
                        val scale = calculateScale(bitmap, currentPageWidth, currentPageHeight)
                        val scaledWidth = bitmap.width * scale
                        val scaledHeight = bitmap.height * scale
                    
                        val left = (currentPageWidth - scaledWidth) / 2
                        val top = (currentPageHeight - scaledHeight) / 2
                    
                        val matrix = android.graphics.Matrix()
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(left, top)
                    
                        canvas.drawBitmap(bitmap, matrix, paint)
                    }
                
                    // Finish page
                    pdfDocument.finishPage(page)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    bitmap?.recycle()
                }
            }
            
            // Save PDF to file
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val pdfFile = File(outputDir, "$fileName.pdf")
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            
            Result.success(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Create an encrypted PDF with password protection
     */
    suspend fun createEncryptedPDF(
        images: List<ImageItem>,
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        outputDir: File,
        password: String,
        compressionLevel: CompressionLevel
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // First create a regular PDF
            val tempFile = File(outputDir, "${fileName}_temp.pdf")
            val pdfDocument = PdfDocument()
            val paint = Paint()
            
            val basePageWidth = if (pageSize == PageSize.ORIGINAL) 0f else {
                 if (orientation == Orientation.PORTRAIT) pageSize.width else pageSize.height
            }
            val basePageHeight = if (pageSize == PageSize.ORIGINAL) 0f else {
                 if (orientation == Orientation.PORTRAIT) pageSize.height else pageSize.width
            }
            
            // Determine max dimension based on compression level
            val maxDimension = when (compressionLevel) {
                CompressionLevel.Original -> 2500
                CompressionLevel.High -> 2000
                CompressionLevel.Medium -> 1500
                CompressionLevel.Low -> 1000
            }
            
            images.sortedBy { it.order }.forEachIndexed { index, imageItem ->
                var bitmap: Bitmap? = null
                try {
                     bitmap = if (pageSize == PageSize.ORIGINAL) {
                        loadOptimizedBitmap(imageItem.uri, maxDimension, maxDimension)
                    } else {
                        val targetWidth = min(basePageWidth.toInt(), maxDimension)
                        val targetHeight = min(basePageHeight.toInt(), maxDimension)
                        loadOptimizedBitmap(imageItem.uri, targetWidth, targetHeight)
                    }
                    
                    if (bitmap == null) return@forEachIndexed
                    
                    val currentPageWidth = if (pageSize == PageSize.ORIGINAL) bitmap.width.toFloat() else basePageWidth
                    val currentPageHeight = if (pageSize == PageSize.ORIGINAL) bitmap.height.toFloat() else basePageHeight
                
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        currentPageWidth.toInt(),
                        currentPageHeight.toInt(),
                        index + 1
                    ).create()
                
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas
                
                    if (pageSize == PageSize.ORIGINAL) {
                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
                    } else {
                        val scale = calculateScale(bitmap, currentPageWidth, currentPageHeight)
                        val scaledWidth = bitmap.width * scale
                        val scaledHeight = bitmap.height * scale
                    
                        val left = (currentPageWidth - scaledWidth) / 2
                        val top = (currentPageHeight - scaledHeight) / 2
                    
                        val matrix = android.graphics.Matrix()
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(left, top)
                    
                        canvas.drawBitmap(bitmap, matrix, paint)
                    }
                    pdfDocument.finishPage(page)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    bitmap?.recycle()
                }
            }
            
            // Save temporary PDF
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            FileOutputStream(tempFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            // Now encrypt the PDF using iText
            val encryptedFile = File(outputDir, "$fileName.pdf")
            encryptPDFWithiText(tempFile, encryptedFile, password)
            
            // Delete temporary file
            tempFile.delete()
            
            Result.success(encryptedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Encrypt a PDF file using iText library
     */
    private fun encryptPDFWithiText(inputFile: File, outputFile: File, password: String) {
        try {
            val reader = com.itextpdf.kernel.pdf.PdfReader(inputFile)
            val writer = com.itextpdf.kernel.pdf.PdfWriter(
                outputFile.absolutePath,
                com.itextpdf.kernel.pdf.WriterProperties().setStandardEncryption(
                    password.toByteArray(),
                    password.toByteArray(),
                    com.itextpdf.kernel.pdf.EncryptionConstants.ALLOW_PRINTING,
                    com.itextpdf.kernel.pdf.EncryptionConstants.ENCRYPTION_AES_256
                )
            )
            
            val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(reader, writer)
            pdfDoc.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to encrypt PDF: ${e.message}")
        }
    }
    
    private fun loadFullBitmap(uri: Uri): Bitmap? {
        // Redirect to optimized loading with very large max dimensions to keep "full" size but safe
        // Or simply use the compression logic with a high limit (2500px is good for "Original" to avoid OOM)
        return loadOptimizedBitmap(uri, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
    }

    private fun loadOptimizedBitmap(uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            val newInputStream: InputStream? = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
            newInputStream?.close()
            
            // Handle Rotation
            if (bitmap != null) {
                val rotation = getRotationFromExif(uri)
                if (rotation != 0) {
                    val matrix = android.graphics.Matrix()
                    matrix.postRotate(rotation.toFloat())
                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                    if (rotatedBitmap != bitmap) {
                        bitmap.recycle()
                        bitmap = rotatedBitmap
                    }
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getRotationFromExif(uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun calculateScale(bitmap: Bitmap, pageWidth: Float, pageHeight: Float): Float {
        val widthScale = pageWidth / bitmap.width
        val heightScale = pageHeight / bitmap.height
        return min(widthScale, heightScale) // Fit within page
    }
}
