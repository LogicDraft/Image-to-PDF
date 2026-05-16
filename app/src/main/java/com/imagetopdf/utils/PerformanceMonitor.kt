package com.imagetopdf.utils

import android.view.Choreographer
import android.util.Log

/**
 * Performance monitoring utility for tracking FPS and frame drops
 * Optimized for 120fps+ displays
 */
object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    private const val TARGET_FPS = 120
    private const val FRAME_TIME_THRESHOLD_MS = 1000.0 / TARGET_FPS // ~8.33ms for 120fps
    
    private var frameCallback: Choreographer.FrameCallback? = null
    private var lastFrameTime: Long = 0
    private var frameCount = 0
    private var droppedFrames = 0
    private var totalFrameTime = 0L
    private var isMonitoring = false
    
    /**
     * Start monitoring FPS
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        lastFrameTime = System.nanoTime()
        frameCount = 0
        droppedFrames = 0
        totalFrameTime = 0
        
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isMonitoring) return
                
                val currentTime = System.nanoTime()
                val frameTime = (currentTime - lastFrameTime) / 1_000_000.0 // Convert to ms
                
                frameCount++
                totalFrameTime += (currentTime - lastFrameTime)
                
                // Check if frame was dropped (took longer than target frame time)
                if (frameTime > FRAME_TIME_THRESHOLD_MS * 1.5) {
                    droppedFrames++
                    Log.w(TAG, "Frame drop detected: ${frameTime}ms (target: ${FRAME_TIME_THRESHOLD_MS}ms)")
                }
                
                // Log FPS every second
                if (frameCount % TARGET_FPS == 0) {
                    val avgFrameTime = (totalFrameTime / frameCount) / 1_000_000.0
                    val currentFPS = 1000.0 / avgFrameTime
                    val dropRate = (droppedFrames.toFloat() / frameCount) * 100
                    
                    Log.d(TAG, "FPS: ${"%.2f".format(currentFPS)} | Avg Frame Time: ${"%.2f".format(avgFrameTime)}ms | Drop Rate: ${"%.2f".format(dropRate)}%")
                }
                
                lastFrameTime = currentTime
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(frameCallback!!)
        Log.d(TAG, "Performance monitoring started (Target: ${TARGET_FPS}fps)")
    }
    
    /**
     * Stop monitoring FPS
     */
    fun stopMonitoring() {
        isMonitoring = false
        frameCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
        }
        
        if (frameCount > 0) {
            val avgFrameTime = (totalFrameTime / frameCount) / 1_000_000.0
            val avgFPS = 1000.0 / avgFrameTime
            val dropRate = (droppedFrames.toFloat() / frameCount) * 100
            
            Log.d(TAG, "=== Performance Summary ===")
            Log.d(TAG, "Average FPS: ${"%.2f".format(avgFPS)}")
            Log.d(TAG, "Average Frame Time: ${"%.2f".format(avgFrameTime)}ms")
            Log.d(TAG, "Total Frames: $frameCount")
            Log.d(TAG, "Dropped Frames: $droppedFrames (${"%.2f".format(dropRate)}%)")
            Log.d(TAG, "==========================")
        }
        
        frameCallback = null
    }
    
    /**
     * Get current FPS estimate
     */
    fun getCurrentFPS(): Float {
        if (frameCount == 0) return 0f
        val avgFrameTime = (totalFrameTime / frameCount) / 1_000_000.0
        return (1000.0 / avgFrameTime).toFloat()
    }
    
    /**
     * Get frame drop percentage
     */
    fun getDropRate(): Float {
        if (frameCount == 0) return 0f
        return (droppedFrames.toFloat() / frameCount) * 100
    }
}
