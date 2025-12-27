package com.example.camrecorder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
// ... (keep imports) ...

class GalleryActivity : AppCompatActivity() {
    // Change this to a function so it always checks for new files
    private fun getVideoFiles(): List<File> {
        val videoDir = getExternalFilesDir(null)
        return videoDir?.listFiles { file ->
            // Only include non-empty MP4 files
            file.extension == "mp4" && file.length() > 0
        }?.sortedByDescending { it.lastModified() } // Sort by timestamp, newest first
            ?.toList() ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        // Initial load: Show all videos found
        val currentFiles = getVideoFiles()
        updateList(currentFiles)

        findViewById<Button>(R.id.btnFront).setOnClickListener {
            val filtered = getVideoFiles().filter { it.name.contains("front", ignoreCase = true) }
            updateList(filtered)
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            val filtered = getVideoFiles().filter { it.name.contains("back", ignoreCase = true) }
            updateList(filtered)
        }
    }

    private fun updateList(files: List<File>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvGallery)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (files.isEmpty()) {
            // This is for your debugging - if it's empty, we should know
            Toast.makeText(this, "No videos found", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = VideoAdapter(files) { file ->
            openVideo(file)
        }
    }

    private fun openVideo(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening video", Toast.LENGTH_SHORT).show()
        }
    }
}