package com.imagetopdf

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.imagetopdf.utils.ThemeManager

class ImageToPDFApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Enable Material You dynamic colors on Android 12+
        val savedTheme = ThemeManager.getThemePreference(this)
        ThemeManager.applyTheme(savedTheme)

        val savedColor = ThemeManager.getColorPreference(this)
        if (savedColor == ThemeManager.ThemeColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}
