package com.imagetopdf.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

import com.imagetopdf.R

object ThemeManager {

    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_THEME_COLOR = "theme_color"

    enum class ThemeMode(val value: String) {
        LIGHT("light"),
        DARK("dark"),
        SYSTEM("system");

        companion object {
            fun fromValue(value: String): ThemeMode {
                return values().find { it.value == value } ?: SYSTEM
            }
        }
    }

    enum class ThemeColor(val value: String, val themeResId: Int) {
        DYNAMIC("dynamic", R.style.Theme_ImageToPDF),
        BLUE("blue", R.style.Theme_ImageToPDF_Blue),
        RED("red", R.style.Theme_ImageToPDF_Red),
        GREEN("green", R.style.Theme_ImageToPDF_Green),
        ORANGE("orange", R.style.Theme_ImageToPDF_Orange),
        PURPLE("purple", R.style.Theme_ImageToPDF_Purple);

        companion object {
            fun fromValue(value: String): ThemeColor {
                return values().find { it.value == value } ?: DYNAMIC
            }
        }
    }

    fun applyTheme(mode: ThemeMode) {
        when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveThemePreference(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.value).apply()
    }

    fun getThemePreference(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.value)
        return when (value) {
            ThemeMode.LIGHT.value -> ThemeMode.LIGHT
            ThemeMode.DARK.value -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    fun saveColorPreference(context: Context, color: ThemeColor) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_COLOR, color.value).apply()
    }

    fun getColorPreference(context: Context): ThemeColor {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_THEME_COLOR, ThemeColor.DYNAMIC.value)
        return ThemeColor.fromValue(value ?: ThemeColor.DYNAMIC.value)
    }
}
