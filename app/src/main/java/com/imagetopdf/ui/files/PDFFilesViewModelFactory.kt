package com.imagetopdf.ui.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imagetopdf.data.repository.PDFRepository

class PDFFilesViewModelFactory(
    private val repository: PDFRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PDFFilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PDFFilesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
