package com.imagetopdf.data.model

enum class PageSize(val width: Float, val height: Float) {
    A4(595f, 842f),
    LETTER(612f, 792f),
    LEGAL(612f, 1008f),
    ORIGINAL(0f, 0f); // Width and Height will be dynamic based on image
    
    fun getDisplayName(): String = when (this) {
        A4 -> "A4"
        LETTER -> "Letter"
        LEGAL -> "Legal"
        ORIGINAL -> "Original"
    }
}

enum class Orientation {
    PORTRAIT,
    LANDSCAPE;
    
    fun getDisplayName(): String = when (this) {
        PORTRAIT -> "Portrait"
        LANDSCAPE -> "Landscape"
    }
}
