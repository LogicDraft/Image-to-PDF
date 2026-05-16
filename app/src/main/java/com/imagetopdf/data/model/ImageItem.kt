package com.imagetopdf.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageItem(
    val uri: Uri,
    val displayName: String,
    var order: Int = 0,
    var isSelected: Boolean = false
) : Parcelable, Comparable<ImageItem> {
    override fun compareTo(other: ImageItem): Int {
        return order.compareTo(other.order)
    }
}
