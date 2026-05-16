package com.imagetopdf.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imagetopdf.data.repository.PDFRepository

class ImagePreviewViewModelFactory(
    private val repository: PDFRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImagePreviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImagePreviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
