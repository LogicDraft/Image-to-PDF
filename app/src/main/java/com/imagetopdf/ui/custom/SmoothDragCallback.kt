package com.imagetopdf.ui.custom

import android.graphics.Canvas
import android.view.HapticFeedbackConstants
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.imagetopdf.utils.HapticUtils

/**
 * Enhanced ItemTouchHelper.Callback for smooth drag and drop animations
 */
abstract class SmoothDragCallback : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    0
) {
    
    private var draggedViewHolder: RecyclerView.ViewHolder? = null
    
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                draggedViewHolder = viewHolder
                viewHolder?.itemView?.let { view ->
                    // Animate elevation and scale
                    view.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .alpha(0.9f)
                        .translationZ(16f)
                        .setDuration(200)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    
                    // Haptic feedback for drag start
                    HapticUtils.performLongPress(view)
                }
            }
        }
    }
    
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        viewHolder.itemView.let { view ->
            // Animate back to normal state
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .translationZ(0f)
                .setDuration(200)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
            
            // Haptic feedback for drop
            HapticUtils.performGestureEnd(view)
        }
        
        draggedViewHolder = null
    }
    
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Add subtle rotation based on drag direction
            val rotation = dX / 20f
            viewHolder.itemView.rotation = rotation.coerceIn(-5f, 5f)
        }
    }
    
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or 
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        )
    }
}
