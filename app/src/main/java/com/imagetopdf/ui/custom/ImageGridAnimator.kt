package com.imagetopdf.ui.custom

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.imagetopdf.utils.HighRefreshInterpolators

/**
 * Custom ItemAnimator for smooth image grid animations
 * Optimized for 120fps+ high refresh rate displays
 */
class ImageGridAnimator : DefaultItemAnimator() {
    
    private val ultraSmoothInterpolator = HighRefreshInterpolators.UltraSmoothEaseInOut()
    
    init {
        // Reduced durations for 120fps+ displays
        addDuration = 250
        removeDuration = 200
        moveDuration = 250
        changeDuration = 200
    }
    
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        holder.itemView.scaleX = 0.8f
        holder.itemView.scaleY = 0.8f
        holder.itemView.translationY = 50f
        
        // Enable hardware layer for smooth animation
        holder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        holder.itemView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(addDuration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                holder.itemView.setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .setListener(null)
            .start()
        
        return super.animateAdd(holder)
    }
    
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        holder.itemView.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .translationY(-50f)
            .setDuration(removeDuration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                holder.itemView.setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .setListener(null)
            .start()
        
        return super.animateRemove(holder)
    }
    
    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        view.animate()
            .translationX(0f)
            .translationY(0f)
            .setDuration(moveDuration)
            .setInterpolator(ultraSmoothInterpolator)
            .withEndAction {
                view.setLayerType(View.LAYER_TYPE_NONE, null)
            }
            .setListener(null)
            .start()
        
        return super.animateMove(holder, fromX, fromY, toX, toY)
    }
}
