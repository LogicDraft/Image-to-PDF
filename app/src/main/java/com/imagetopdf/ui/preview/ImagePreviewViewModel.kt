package com.imagetopdf.ui.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imagetopdf.data.model.ImageItem
import com.imagetopdf.data.model.Orientation
import com.imagetopdf.data.model.PDFDocument
import com.imagetopdf.data.model.PageSize
import com.imagetopdf.data.repository.PDFRepository
import kotlinx.coroutines.launch

class ImagePreviewViewModel(private val repository: PDFRepository) : ViewModel() {

    private val _images = MutableLiveData<List<ImageItem>>()
    val images: LiveData<List<ImageItem>> = _images

    private val _isCreating = MutableLiveData<Boolean>()
    val isCreating: LiveData<Boolean> = _isCreating

    private val _creationResult = MutableLiveData<Result<PDFDocument>>()
    val creationResult: LiveData<Result<PDFDocument>> = _creationResult

    fun setImages(imageList: List<ImageItem>) {
        _images.value = imageList.mapIndexed { index, item ->
            item.copy(order = index)
        }
    }

    fun addImages(newImages: List<ImageItem>) {
        val currentList = _images.value?.toMutableList() ?: mutableListOf()
        val startingIndex = currentList.size
        
        val newItemsWithOrder = newImages.mapIndexed { index, item ->
            item.copy(order = startingIndex + index)
        }
        
        currentList.addAll(newItemsWithOrder)
        _images.value = currentList
    }

    fun removeImage(position: Int) {
        val currentList = _images.value?.toMutableList() ?: return
        if (position < currentList.size) {
            currentList.removeAt(position)
            _images.value = currentList.mapIndexed { index, item ->
                item.copy(order = index)
            }
        }
    }

    fun updateImageOrder(reorderedList: List<ImageItem>) {
        _images.value = reorderedList
    }

    fun createPDF(
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        compressionLevel: com.imagetopdf.utils.PDFGenerator.CompressionLevel = com.imagetopdf.utils.PDFGenerator.CompressionLevel.Original
    ) {
        val imageList = _images.value
        if (imageList.isNullOrEmpty()) {
            _creationResult.value = Result.failure(Exception("No images selected"))
            return
        }

        if (fileName.isBlank()) {
            _creationResult.value = Result.failure(Exception("File name is required"))
            return
        }

        _isCreating.value = true

        viewModelScope.launch {
            val result = repository.createPDF(
                images = imageList,
                fileName = fileName,
                pageSize = pageSize,
                orientation = orientation,
                compressionLevel = compressionLevel
            )
            _isCreating.value = false
            _creationResult.value = result
        }
    }
    
    fun createEncryptedPDF(
        fileName: String,
        pageSize: PageSize,
        orientation: Orientation,
        password: String,
        compressionLevel: com.imagetopdf.utils.PDFGenerator.CompressionLevel = com.imagetopdf.utils.PDFGenerator.CompressionLevel.Original
    ) {
        val imageList = _images.value
        if (imageList.isNullOrEmpty()) {
            _creationResult.value = Result.failure(Exception("No images selected"))
            return
        }

        if (fileName.isBlank()) {
            _creationResult.value = Result.failure(Exception("File name is required"))
            return
        }

        _isCreating.value = true

        viewModelScope.launch {
            val result = repository.createEncryptedPDF(
                images = imageList,
                fileName = fileName,
                pageSize = pageSize,
                orientation = orientation,
                password = password,
                compressionLevel = compressionLevel
            )
            _isCreating.value = false
            _creationResult.value = result
        }
    }
}
