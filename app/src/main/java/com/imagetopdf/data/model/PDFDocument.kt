package com.imagetopdf.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class PDFDocument(
    val id: String,
    val name: String,
    val filePath: String,
    val createdDate: Date,
    val fileSize: Long
) : Parcelable {
    fun getFormattedSize(): String {
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$fileSize B"
        }
    }
    
    fun getFormattedDate(): String {
        val now = Date()
        val diff = now.time - createdDate.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    }
}
