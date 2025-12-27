package com.example.camrecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class VideoAdapter(
    private val videoFiles: List<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Uses the standard Android ID for a simple list item
        val fileName: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        // FIX: Added 'android.' prefix to access the system resource
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val file = videoFiles[position]
        holder.fileName.text = file.name
        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount(): Int = videoFiles.size
}