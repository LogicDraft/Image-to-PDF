package com.imagetopdf.utils

import android.view.animation.Interpolator
import kotlin.math.pow

/**
 * Custom interpolators optimized for 120fps+ displays
 * Smoother curves for high refresh rate screens
 */
object HighRefreshInterpolators {
    
    /**
     * Ultra-smooth ease out for 120fps+
     * Optimized for high refresh rate displays
     */
    class UltraSmoothEaseOut : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return 1f - (1f - input).pow(3f)
        }
    }
    
    /**
     * Ultra-smooth ease in for 120fps+
     */
    class UltraSmoothEaseIn : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return input.pow(3f)
        }
    }
    
    /**
     * Ultra-smooth ease in-out for 120fps+
     * Provides the smoothest motion for high refresh displays
     */
    class UltraSmoothEaseInOut : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return if (input < 0.5f) {
                4f * input * input * input
            } else {
                1f - (-2f * input + 2f).pow(3f) / 2f
            }
        }
    }
    
    /**
     * Spring interpolator for natural motion at 120fps+
     * Adds subtle bounce for premium feel
     */
    class SpringInterpolator(
        private val tension: Float = 0.3f,
        private val friction: Float = 0.7f
    ) : Interpolator {
        override fun getInterpolation(input: Float): Float {
            val t = input
            return (1f - (1f - t).pow(2f)) * (1f + tension * kotlin.math.sin(t * Math.PI.toFloat() * friction))
        }
    }
    
    /**
     * Anticipate interpolator for 120fps+
     * Slight pullback before forward motion
     */
    class AnticipateInterpolator(private val tension: Float = 2f) : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return input * input * ((tension + 1f) * input - tension)
        }
    }
    
    /**
     * Overshoot interpolator for 120fps+
     * Slight overshoot at the end
     */
    class OvershootInterpolator(private val tension: Float = 2f) : Interpolator {
        override fun getInterpolation(input: Float): Float {
            val t = input - 1f
            return t * t * ((tension + 1f) * t + tension) + 1f
        }
    }
}
