package com.example.camrecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
// Updated VideoAdapter.kt
class VideoAdapter(
    private val videoFiles: List<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Change this to use your new ID: tvFileName
        val fileName: TextView = view.findViewById(R.id.tvFileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        // Change simple_list_item_1 to your new custom layout: R.layout.item_video
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val file = videoFiles[position]
        holder.fileName.text = file.name
        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount(): Int = videoFiles.size
}