package com.imagetopdf.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private const val APP_FOLDER_NAME = "ImageToPDF"

    /**
     * Get the default directory for saving PDFs
     * Location: /Downloads/ImageToPDF/
     */
    fun getDefaultPDFDirectory(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, APP_FOLDER_NAME)
    }

    /**
     * Create the default directory if it doesn't exist
     */
    fun createDefaultDirectory(): Boolean {
        val directory = getDefaultPDFDirectory()
        return if (!directory.exists()) {
            directory.mkdirs()
        } else {
            true
        }
    }

    /**
     * Generate smart file name based on current date
     * Format: ImageToPDF_DD_MMM_YYYY
     */
    fun generateSmartFileName(): String {
        val dateFormat = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault())
        val date = dateFormat.format(Date())
        return "ImageToPDF_$date"
    }

    /**
     * Generate alternative smart file name with different format
     * Format: Scanned_Notes_YYYY_MM_DD
     */
    fun generateAlternativeFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault())
        val date = dateFormat.format(Date())
        return "Scanned_Notes_$date"
    }

    /**
     * Ensure the file name is unique by appending a counter if needed
     */
    fun ensureUniqueFileName(baseName: String, directory: File): String {
        var fileName = "$baseName.pdf"
        var counter = 1
        
        while (File(directory, fileName).exists()) {
            fileName = "${baseName}_$counter.pdf"
            counter++
        }
        
        return fileName
    }

    /**
     * Validate file name - remove invalid characters
     */
    fun validateFileName(fileName: String): String {
        // Remove invalid characters for file names
        var validated = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        
        // Remove .pdf extension if present (will be added later)
        if (validated.endsWith(".pdf", ignoreCase = true)) {
            validated = validated.substring(0, validated.length - 4)
        }
        
        // Trim whitespace
        validated = validated.trim()
        
        // If empty after validation, use default
        if (validated.isEmpty()) {
            validated = generateSmartFileName()
        }
        
        return validated
    }

    /**
     * Generate a unique file ID
     */
    fun generateFileId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Format file size in human-readable format
     */
    fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeInBytes B"
        }
    }

    /**
     * Sanitize file name for safe storage
     */
    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    /**
     * Get file extension
     */
    fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(lastDot + 1) else ""
    }

    /**
     * Get file name without extension
     */
    fun getFileNameWithoutExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(0, lastDot) else fileName
    }
    fun getFileName(context: Context, uri: android.net.Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
