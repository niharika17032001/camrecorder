package com.example.camrecorder

import android.app.AlertDialog
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

class GalleryActivity : AppCompatActivity() {

    private fun getVideoFiles(): List<File> {
        val videoDir = getExternalFilesDir(null)
        return videoDir?.listFiles { file ->
            file.extension == "mp4" && file.length() > 0
        }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        refreshUI() // Initial load
    }

    private fun refreshUI() {
        val currentFiles = getVideoFiles()
        updateList(currentFiles)

        findViewById<Button>(R.id.btnFront).setOnClickListener {
            updateList(getVideoFiles().filter { it.name.contains("front", ignoreCase = true) })
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            updateList(getVideoFiles().filter { it.name.contains("back", ignoreCase = true) })
        }
    }

    private fun updateList(files: List<File>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvGallery)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pass the long-click handler to show the delete dialog
        recyclerView.adapter = VideoAdapter(files,
            { file -> openVideo(file) },
            { file -> showDeleteDialog(file) }
        )
    }

    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Delete Video")
            .setMessage("Are you sure you want to delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.exists() && file.delete()) {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    refreshUI() // Refresh the list after deletion
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openVideo(file: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
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