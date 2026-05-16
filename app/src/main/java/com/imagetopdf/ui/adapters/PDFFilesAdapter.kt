package com.imagetopdf.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imagetopdf.R
import com.imagetopdf.data.model.PDFDocument

class PDFFilesAdapter(
    private val onOpenClick: (PDFDocument) -> Unit,
    private val onShareClick: (PDFDocument) -> Unit,
    private val onDeleteClick: (PDFDocument) -> Unit
) : ListAdapter<PDFDocument, PDFFilesAdapter.PDFViewHolder>(PDFDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf_file, parent, false)
        return PDFViewHolder(view)
    }

    override fun onBindViewHolder(holder: PDFViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PDFViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFileInfo: TextView = itemView.findViewById(R.id.tvFileInfo)
        private val btnOpen: ImageButton = itemView.findViewById(R.id.btnOpen)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btnShare)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(pdfDocument: PDFDocument) {
            tvFileName.text = pdfDocument.name
            tvFileInfo.text = "${pdfDocument.getFormattedSize()} • ${pdfDocument.getFormattedDate()}"

            btnOpen.setOnClickListener { onOpenClick(pdfDocument) }
            btnShare.setOnClickListener { onShareClick(pdfDocument) }
            btnDelete.setOnClickListener { onDeleteClick(pdfDocument) }
        }
    }

    private class PDFDiffCallback : DiffUtil.ItemCallback<PDFDocument>() {
        override fun areItemsTheSame(oldItem: PDFDocument, newItem: PDFDocument): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PDFDocument, newItem: PDFDocument): Boolean {
            return oldItem == newItem
        }
    }
}
