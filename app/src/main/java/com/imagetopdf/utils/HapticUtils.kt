package com.imagetopdf.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility class for haptic feedback throughout the app
 */
object HapticUtils {
    
    /**
     * Light haptic feedback for subtle interactions
     * Use for: Image selection, checkbox toggles
     */
    fun performLightClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
    
    /**
     * Medium haptic feedback for standard interactions
     * Use for: Button clicks, drag start
     */
    fun performMediumClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CONTEXT_CLICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    
    /**
     * Long press haptic feedback
     * Use for: Drag and drop start, long press actions
     */
    fun performLongPress(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
    
    /**
     * Success haptic feedback
     * Use for: PDF creation success, save complete
     */
    fun performSuccess(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CONFIRM,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } else {
            // Fallback to double light click for success feel
            performLightClick(view)
            view.postDelayed({
                performLightClick(view)
            }, 50)
        }
    }
    
    /**
     * Error/rejection haptic feedback
     * Use for: Validation errors, failed operations
     */
    fun performError(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(
                HapticFeedbackConstants.REJECT,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } else {
            // Fallback to keyboard tap for error
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
    
    /**
     * Gesture end haptic feedback
     * Use for: Drag drop complete, swipe complete
     */
    fun performGestureEnd(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(
                HapticFeedbackConstants.GESTURE_END,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } else {
            performMediumClick(view)
        }
    }
    
    /**
     * Keyboard tap haptic feedback
     * Use for: Text input, keyboard interactions
     */
    fun performKeyboardTap(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}
