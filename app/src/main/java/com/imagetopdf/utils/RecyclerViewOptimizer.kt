package com.imagetopdf.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView optimization utilities for 120fps+ performance
 */
object RecyclerViewOptimizer {
    
    /**
     * Apply all optimizations for 120fps+ performance
     */
    fun optimize(recyclerView: RecyclerView) {
        recyclerView.apply {
            // Fixed size optimization
            setHasFixedSize(true)
            
            // Increase view cache size for smoother scrolling
            setItemViewCacheSize(20)
            
            // Enable drawing cache for better performance
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            
            // Reduce overdraw
            setWillNotDraw(false)
            
            // Nested scrolling optimization
            isNestedScrollingEnabled = true
            
            // Prefetch optimization for smoother scrolling
            layoutManager?.isItemPrefetchEnabled = true
            
            // Hardware layer during scroll for 120fps+
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING,
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            // Enable hardware layer during scroll
                            recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // Disable hardware layer when idle
                            recyclerView.setLayerType(View.LAYER_TYPE_NONE, null)
                        }
                    }
                }
            })
        }
    }
    
    /**
     * Optimize adapter for better performance
     */
    fun optimizeAdapter(adapter: RecyclerView.Adapter<*>) {
        // Enable stable IDs if possible
        adapter.setHasStableIds(false) // Set to true if you implement getItemId()
    }
}
