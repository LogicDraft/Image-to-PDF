package com.imagetopdf.utils

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * Utility class for smooth, premium animations throughout the app
 * Optimized for 120fps+ high refresh rate displays
 */
object AnimationUtils {
    
    private const val DURATION_SHORT = 100L
    private const val DURATION_MEDIUM = 200L
    private const val DURATION_LONG = 300L
    
    // Use ultra-smooth interpolator for 120fps+
    private val ultraSmoothInterpolator = HighRefreshInterpolators.UltraSmoothEaseInOut()
    
    /**
     * Animate button press with scale effect
     * Optimized for 120fps+ displays
     */
    fun View.animatePress(onComplete: () -> Unit = {}) {
        // Enable hardware layer for smooth animation
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(DURATION_SHORT)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(DURATION_SHORT)
                    .setInterpolator(ultraSmoothInterpolator)
                    .withEndAction {
                        setLayerType(View.LAYER_TYPE_NONE, null)
                        onComplete()
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Fade in view with scale
     * 120fps+ optimized
     */
    fun View.fadeIn(duration: Long = DURATION_MEDIUM) {
        alpha = 0f
        scaleX = 0.95f
        scaleY = 0.95f
        visibility = View.VISIBLE
        
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .start()
    }
    
    /**
     * Fade out view with scale
     * 120fps+ optimized
     */
    fun View.fadeOut(duration: Long = DURATION_MEDIUM, onComplete: () -> Unit = {}) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .alpha(0f)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(duration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                visibility = View.GONE
                setLayerType(View.LAYER_TYPE_NONE, null)
                onComplete()
            }
            .start()
    }
    
    /**
     * Slide up animation for bottom sheets
     * 120fps+ optimized
     */
    fun View.slideUp(duration: Long = DURATION_LONG) {
        translationY = height.toFloat()
        alpha = 0f
        visibility = View.VISIBLE
        
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .start()
    }
    
    /**
     * Slide down animation for dismissing
     * 120fps+ optimized
     */
    fun View.slideDown(duration: Long = DURATION_MEDIUM, onComplete: () -> Unit = {}) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .translationY(height.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                visibility = View.GONE
                setLayerType(View.LAYER_TYPE_NONE, null)
                onComplete()
            }
            .start()
    }
    
    /**
     * Animate image selection with scale and alpha
     * 120fps+ optimized
     */
    fun View.animateSelection(isSelected: Boolean) {
        val scale = if (isSelected) 0.9f else 1.0f
        val alpha = if (isSelected) 0.7f else 1.0f
        
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .setDuration(DURATION_MEDIUM)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .start()
    }
    
    /**
     * Bounce animation for success feedback
     * 120fps+ optimized with spring physics
     */
    fun View.bounce() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(DURATION_SHORT)
            .setInterpolator(HighRefreshInterpolators.SpringInterpolator())
            .withEndAction {
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(DURATION_MEDIUM)
                    .setInterpolator(ultraSmoothInterpolator)
                    .withEndAction {
                        setLayerType(View.LAYER_TYPE_NONE, null)
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Shake animation for error feedback
     * 120fps+ optimized
     */
    fun View.shake() {
        val distance = 10f
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .translationX(distance)
            .setDuration(50)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                animate()
                    .translationX(-distance)
                    .setDuration(50)
                    .setInterpolator(ultraSmoothInterpolator)
                    .withEndAction {
                        animate()
                            .translationX(distance)
                            .setDuration(50)
                            .setInterpolator(ultraSmoothInterpolator)
                            .withEndAction {
                                animate()
                                    .translationX(0f)
                                    .setDuration(50)
                                    .setInterpolator(ultraSmoothInterpolator)
                                    .withEndAction {
                                        setLayerType(View.LAYER_TYPE_NONE, null)
                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Pulse animation for attention
     * 120fps+ optimized
     */
    fun View.pulse(repeat: Int = 1) {
        var count = 0
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        fun doPulse() {
            if (count < repeat) {
                animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .alpha(0.8f)
                    .setDuration(DURATION_MEDIUM)
                    .setInterpolator(ultraSmoothInterpolator)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(DURATION_MEDIUM)
                            .setInterpolator(ultraSmoothInterpolator)
                            .withEndAction {
                                count++
                                if (count >= repeat) {
                                    setLayerType(View.LAYER_TYPE_NONE, null)
                                } else {
                                    doPulse()
                                }
                            }
                            .start()
                    }
                    .start()
            }
        }
        doPulse()
    }
    
    /**
     * Rotate animation
     * 120fps+ optimized
     */
    fun View.rotate(degrees: Float, duration: Long = DURATION_MEDIUM) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        animate()
            .rotation(degrees)
            .setDuration(duration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .start()
    }
    
    /**
     * Enable hardware layer for complex animations
     * Automatically disabled after animation completes
     */
    fun View.withHardwareLayer(action: () -> Unit) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        action()
        postDelayed({
            setLayerType(View.LAYER_TYPE_NONE, null)
        }, 500)
    }
}
