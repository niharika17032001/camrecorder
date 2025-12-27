package com.example.camrecorder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val recyclerView = findViewById<RecyclerView>(R.id.rvGallery)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetches mp4 files from the app's internal storage
        val videoDir = getExternalFilesDir(null)
        val videoFiles = videoDir?.listFiles { file -> file.extension == "mp4" }?.toList() ?: emptyList()

        recyclerView.adapter = VideoAdapter(videoFiles) { file: File ->
            openVideo(file)
        }
    }

    private fun openVideo(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}