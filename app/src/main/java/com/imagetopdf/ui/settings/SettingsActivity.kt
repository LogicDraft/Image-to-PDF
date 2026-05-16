package com.imagetopdf.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imagetopdf.R
import com.imagetopdf.databinding.ActivitySettingsBinding
import com.imagetopdf.databinding.DialogThemeSelectorBinding
import com.imagetopdf.databinding.DialogAccentColorBinding
import com.imagetopdf.ui.base.BaseActivity
import com.imagetopdf.utils.ThemeManager
import androidx.core.content.FileProvider
import java.io.File

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val PREFS_NAME = "app_preferences"
    private val KEY_SMART_NAMING = "smart_file_naming"
    private val KEY_PASSWORD_PROTECTION = "enable_password_protection"

    private val backgroundPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Take persistable URI permission
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                // Save URI to preferences
                getSharedPreferences("app_preferences", MODE_PRIVATE).edit()
                    .putString("home_background_uri", uri.toString())
                    .apply()
                
                Toast.makeText(this, "Background updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to set background: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        setupToggles()
        displayVersion()
        displayVersion()
        updateCurrentThemeDisplay()
        setupQualitySelector()
    }

    private fun setupQualitySelector() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val quality = prefs.getString("pdf_quality", "Original") ?: "Original"
        binding.tvQualityValue.text = getQualityText(quality)

        binding.cardQuality.setOnClickListener {
            showQualityDialog()
        }
    }

    private fun getQualityText(quality: String): String {
        return when (quality) {
            "Original" -> getString(R.string.quality_original)
            "High" -> getString(R.string.quality_high)
            "Medium" -> getString(R.string.quality_medium)
            "Low" -> getString(R.string.quality_low)
            else -> getString(R.string.quality_original)
        }
    }

    private fun showQualityDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentQuality = prefs.getString("pdf_quality", "Original") ?: "Original"
        
        val qualities = arrayOf("Original", "High", "Medium", "Low")
        val qualityTitles = arrayOf(
            getString(R.string.quality_original),
            getString(R.string.quality_high),
            getString(R.string.quality_medium),
            getString(R.string.quality_low)
        )
        
        var selectedIndex = qualities.indexOf(currentQuality)
        if (selectedIndex == -1) selectedIndex = 0

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_quality_title)
            .setSingleChoiceItems(qualityTitles, selectedIndex) { dialog, which ->
                 val selectedQuality = qualities[which]
                 prefs.edit()
                     .putString("pdf_quality", selectedQuality)
                     .apply()
                 
                 binding.tvQualityValue.text = getQualityText(selectedQuality)
                 dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.cardChangeMode.setOnClickListener {
            showThemeSelector()
        }
        
        // Accent color listener removed
        
        binding.cardHomeBackground.setOnClickListener {
            try {
                backgroundPickerLauncher.launch("image/*")
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot open image picker", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardFeedback.setOnClickListener {
            sendFeedbackEmail()
        }

        binding.cardShareApp.setOnClickListener {
            shareApp()
        }


    }

    private fun setupToggles() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Smart File Naming toggle
        binding.switchSmartNaming.isChecked = prefs.getBoolean(KEY_SMART_NAMING, true)
        binding.switchSmartNaming.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_SMART_NAMING, isChecked).apply()
        }
        
        // Password Protection toggle
        binding.switchPasswordProtection.isChecked = prefs.getBoolean(KEY_PASSWORD_PROTECTION, false)
        binding.switchPasswordProtection.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_PASSWORD_PROTECTION, isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "Password protection will be available in premium version", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val versionText = getString(R.string.app_version, pInfo.versionName)
            val footer = getString(R.string.credit_footer)
            binding.tvVersion.text = "$versionText\n$footer"
        } catch (e: Exception) {
            binding.tvVersion.text = getString(R.string.app_version, "1.0.0")
        }
    }

    private fun updateCurrentThemeDisplay() {
        val currentTheme = ThemeManager.getThemePreference(this)
        binding.tvCurrentTheme.text = when (currentTheme) {
            ThemeManager.ThemeMode.LIGHT -> getString(R.string.theme_mode_light)
            ThemeManager.ThemeMode.DARK -> getString(R.string.theme_mode_dark)
            ThemeManager.ThemeMode.SYSTEM -> getString(R.string.theme_mode_system)
        }
    }

    private fun showThemeSelector() {
        val dialogBinding = DialogThemeSelectorBinding.inflate(LayoutInflater.from(this))
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Set current selection
        val currentTheme = ThemeManager.getThemePreference(this)
        when (currentTheme) {
            ThemeManager.ThemeMode.LIGHT -> dialogBinding.radioLight.isChecked = true
            ThemeManager.ThemeMode.DARK -> dialogBinding.radioDark.isChecked = true
            ThemeManager.ThemeMode.SYSTEM -> dialogBinding.radioSystem.isChecked = true
        }

        // Handle Save button
        dialogBinding.btnSave.setOnClickListener {
            val selectedTheme = when (dialogBinding.radioGroupTheme.checkedRadioButtonId) {
                R.id.radioLight -> ThemeManager.ThemeMode.LIGHT
                R.id.radioDark -> ThemeManager.ThemeMode.DARK
                R.id.radioSystem -> ThemeManager.ThemeMode.SYSTEM
                else -> ThemeManager.ThemeMode.SYSTEM
            }

            ThemeManager.saveThemePreference(this, selectedTheme)
            ThemeManager.applyTheme(selectedTheme)
            updateCurrentThemeDisplay()
            bottomSheetDialog.dismiss()
        }

        // Handle Cancel button
        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("gowdagowtham7930@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for Image to PDF")
        }
        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            val shareMessage = "Check out ${getString(R.string.app_name)}: https://play.google.com/store/apps/details?id=${packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.setting_share_app)))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to share app", Toast.LENGTH_SHORT).show()
        }
    }


}
