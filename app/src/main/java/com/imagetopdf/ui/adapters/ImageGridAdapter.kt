package com.imagetopdf.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imagetopdf.R
import com.imagetopdf.data.model.ImageItem

class ImageGridAdapter(
    private val onImageClick: (ImageItem, Int) -> Unit
) : ListAdapter<ImageItem, ImageGridAdapter.ImageViewHolder>(ImageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_grid, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        private val viewOverlay: View = itemView.findViewById(R.id.viewOverlay)

        fun bind(imageItem: ImageItem, position: Int) {
            Glide.with(itemView.context)
                .load(imageItem.uri)
                .centerCrop()
                .placeholder(R.color.image_placeholder)
                .error(R.color.image_placeholder)
                .into(ivImage)

            cbSelect.isChecked = imageItem.isSelected
            viewOverlay.visibility = if (imageItem.isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onImageClick(imageItem, position)
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
}
