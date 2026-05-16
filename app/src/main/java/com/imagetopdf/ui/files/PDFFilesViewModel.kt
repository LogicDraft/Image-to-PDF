package com.imagetopdf.ui.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imagetopdf.data.model.PDFDocument
import com.imagetopdf.data.repository.PDFRepository
import kotlinx.coroutines.launch

class PDFFilesViewModel(private val repository: PDFRepository) : ViewModel() {

    private val _pdfFiles = MutableLiveData<List<PDFDocument>>()
    val pdfFiles: LiveData<List<PDFDocument>> = _pdfFiles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPDFs() {
        _isLoading.value = true
        viewModelScope.launch {
            val pdfs = repository.getAllPDFs()
            _pdfFiles.value = pdfs
            _isLoading.value = false
        }
    }

    fun deletePDF(pdfDocument: PDFDocument) {
        viewModelScope.launch {
            val success = repository.deletePDF(pdfDocument)
            if (success) {
                loadPDFs()
            }
        }
    }
}
