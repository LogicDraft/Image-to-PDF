package com.imagetopdf.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.imagetopdf.utils.ThemeManager

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme color before super.onCreate and setContentView
        // Apply theme color removed
        // ThemeManager.applyTheme(ThemeManager.getThemePreference(this))
        
        // Apply Mode (Day/Night) - although this is usually global, good to ensure
        // specific activities don't override it unexpectedly, or just let App/ThemeManager handle it globally.
        // ThemeManager.applyTheme(ThemeManager.getThemePreference(this)) 
        // We already apply mode globally in App/MainActivity, but let's be safe:
        
        super.onCreate(savedInstanceState)
    }
}
