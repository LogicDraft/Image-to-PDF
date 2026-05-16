package com.imagetopdf.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imagetopdf.R
import com.imagetopdf.data.model.ImageItem
import java.util.Collections

class ImagePreviewAdapter(
    private val onRemoveClick: (ImageItem, Int) -> Unit
) : ListAdapter<ImageItem, ImagePreviewAdapter.PreviewViewHolder>(ImageDiffCallback()) {

    private val items = mutableListOf<ImageItem>()

    override fun submitList(list: List<ImageItem>?) {
        items.clear()
        if (list != null) {
            items.addAll(list)
        }
        super.submitList(list?.toList())
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getItems(): List<ImageItem> {
        return items.mapIndexed { index, item ->
            item.copy(order = index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_preview, parent, false)
        return PreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(imageItem: ImageItem, position: Int) {
            Glide.with(itemView.context)
                .load(imageItem.uri)
                .centerCrop()
                .into(ivImage)

            btnRemove.setOnClickListener {
                onRemoveClick(imageItem, position)
            }
        }
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem == newItem
        }
    }

    class ItemTouchHelperCallback(
        private val adapter: ImagePreviewAdapter
    ) : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Not used
        }

        override fun isLongPressDragEnabled(): Boolean = true
    }
}
