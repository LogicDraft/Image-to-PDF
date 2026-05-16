# Image to PDF - Native Android App

A clean, minimal, offline-first Android application for converting multiple images into PDF documents.

## Features

- ✅ **Multi-Image Selection**: Select multiple images from gallery
- ✅ **Camera Integration**: Capture photos directly from the app
- ✅ **Drag & Drop Reordering**: Rearrange images before PDF creation
- ✅ **Customizable PDF Settings**: Choose page size (A4, Letter, Legal) and orientation
- ✅ **PDF Management**: View, share, and delete created PDFs
- ✅ **Offline Functionality**: Works completely offline, no internet required
- ✅ **Material Design 3**: Modern UI following Android 16 design guidelines

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35 (Android 16)
- **UI**: Material Design 3 Components
- **Image Loading**: Glide
- **PDF Generation**: Android PdfDocument API

## Project Structure

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### Steps

1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on an emulator or physical device (Android 8.0+)

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Permissions

- **READ_MEDIA_IMAGES** (Android 13+) / **READ_EXTERNAL_STORAGE** (Android 12 and below): Access device images
- **CAMERA**: Capture photos

## App Screens

1. **Home**: Main navigation with three action buttons
2. **Image Selection**: Grid view of device images with multi-select
3. **Image Preview**: Reorder selected images and configure PDF settings
4. **PDF Files**: List of created PDFs with open/share/delete actions
5. **Settings**: App settings and information

## Version

Current Version: **1.0**

## License

This project is created for demonstration purposes.
