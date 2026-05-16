package com.imagetopdf.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.imagetopdf.data.model.ImageItem
import com.imagetopdf.data.model.Orientation
import com.imagetopdf.data.model.PDFDocument
import com.imagetopdf.data.model.PageSize
import com.imagetopdf.utils.FileUtils
import com.imagetopdf.utils.PDFGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class PDFRepository(private val context: Context) {
    
    private val pdfGenerator = PDFGenerator(context)
    private val pdfDirectory: File
        get() {
            // Use default directory: /Downloads/ImageToPDF/
            FileUtils.createDefaultDirectory()
            return FileUtils.getDefaultPDFDirectory()
        }
    
    suspend fun createPDF(
        images: List<ImageItem>,
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        compressionLevel: PDFGenerator.CompressionLevel = PDFGenerator.CompressionLevel.Original
    ): Result<PDFDocument> = withContext(Dispatchers.IO) {
        try {
            val validatedName = FileUtils.validateFileName(fileName)
            val uniqueFileName = FileUtils.ensureUniqueFileName(validatedName, pdfDirectory)
            val fileNameWithoutExt = FileUtils.getFileNameWithoutExtension(uniqueFileName)
            
            val result = pdfGenerator.createPDF(
                images = images,
                fileName = fileNameWithoutExt,
                pageSize = pageSize,
                orientation = orientation,
                outputDir = pdfDirectory,
                compressionLevel = compressionLevel
            )
            
            result.fold(
                onSuccess = { file ->
                    val pdfDocument = PDFDocument(
                        id = FileUtils.generateFileId(),
                        name = file.nameWithoutExtension,
                        filePath = file.absolutePath,
                        createdDate = Date(),
                        fileSize = file.length()
                    )
                    Result.success(pdfDocument)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createEncryptedPDF(
        images: List<ImageItem>,
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        password: String,
        compressionLevel: PDFGenerator.CompressionLevel = PDFGenerator.CompressionLevel.Original
    ): Result<PDFDocument> = withContext(Dispatchers.IO) {
        try {
            val validatedName = FileUtils.validateFileName(fileName)
            val uniqueFileName = FileUtils.ensureUniqueFileName(validatedName, pdfDirectory)
            val fileNameWithoutExt = FileUtils.getFileNameWithoutExtension(uniqueFileName)
            
            val result = pdfGenerator.createEncryptedPDF(
                images = images,
                fileName = fileNameWithoutExt,
                pageSize = pageSize,
                orientation = orientation,
                outputDir = pdfDirectory,
                password = password,
                compressionLevel = compressionLevel
            )
            
            result.fold(
                onSuccess = { file ->
                    val pdfDocument = PDFDocument(
                        id = FileUtils.generateFileId(),
                        name = file.nameWithoutExtension,
                        filePath = file.absolutePath,
                        createdDate = Date(),
                        fileSize = file.length()
                    )
                    Result.success(pdfDocument)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllPDFs(): List<PDFDocument> = withContext(Dispatchers.IO) {
        try {
            pdfDirectory.listFiles()
                ?.filter { it.extension == "pdf" }
                ?.sortedByDescending { it.lastModified() }
                ?.map { file ->
                    PDFDocument(
                        id = file.nameWithoutExtension,
                        name = file.nameWithoutExtension,
                        filePath = file.absolutePath,
                        createdDate = Date(file.lastModified()),
                        fileSize = file.length()
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun deletePDF(pdfDocument: PDFDocument): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(pdfDocument.filePath)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getShareIntent(pdfDocument: PDFDocument): Intent? {
        return try {
            val file = File(pdfDocument.filePath)
            if (!file.exists()) return null
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getOpenIntent(pdfDocument: PDFDocument): Intent? {
        return try {
            val file = File(pdfDocument.filePath)
            if (!file.exists()) return null
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }
}
