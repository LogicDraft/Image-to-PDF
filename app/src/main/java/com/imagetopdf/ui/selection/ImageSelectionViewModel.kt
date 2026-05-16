package com.imagetopdf.ui.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.imagetopdf.data.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageSelectionViewModel : ViewModel() {

    private val _images = MutableLiveData<List<ImageItem>>()
    val images: LiveData<List<ImageItem>> = _images

    private val _selectedImages = MutableLiveData<List<ImageItem>>()
    val selectedImages: LiveData<List<ImageItem>> = _selectedImages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadImages(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val imageList = withContext(Dispatchers.IO) {
                    loadImagesFromDevice(context)
                }
                _images.value = imageList
            } catch (e: Exception) {
                _error.value = "Failed to load images: ${e.message}"
                _images.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadImagesFromDevice(context: Context): List<ImageItem> {
        val imageList = mutableListOf<ImageItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.IS_PENDING} = 0"
        } else {
            null
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                imageList.add(ImageItem(uri, name))
            }
        }

        return imageList
    }

    fun toggleImageSelection(imageItem: ImageItem, position: Int) {
        val currentList = _images.value?.toMutableList() ?: return
        currentList[position] = imageItem.copy(isSelected = !imageItem.isSelected)
        _images.value = currentList
        updateSelectedImages()
    }

    fun selectAll() {
        val currentList = _images.value?.toMutableList() ?: return
        _images.value = currentList.map { it.copy(isSelected = true) }
        updateSelectedImages()
    }

    fun deselectAll() {
        val currentList = _images.value?.toMutableList() ?: return
        _images.value = currentList.map { it.copy(isSelected = false) }
        updateSelectedImages()
    }

    private fun updateSelectedImages() {
        _selectedImages.value = _images.value?.filter { it.isSelected } ?: emptyList()
    }

    fun getSelectedCount(): Int {
        return _images.value?.count { it.isSelected } ?: 0
    }
    fun addSystemImages(context: Context, uris: List<android.net.Uri>) {
        viewModelScope.launch {
            val validImages = uris.distinct().map { uri ->
                val name = com.imagetopdf.utils.FileUtils.getFileName(context, uri) ?: "Image_${System.currentTimeMillis()}"
                ImageItem(uri, name, isSelected = true)
            }
            
            val currentList = _images.value?.toMutableList() ?: mutableListOf()
            currentList.addAll(0, validImages)
            
            _images.value = currentList
            updateSelectedImages()
        }
    }
}
