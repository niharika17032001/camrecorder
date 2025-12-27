package com.example.camrecorder

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraForegroundService : LifecycleService() {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): CameraForegroundService = this@CameraForegroundService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, createNotification("Ready to Record"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else 0
        )
        return super.onStartCommand(intent, flags, startId)
    }

// In CameraForegroundService.kt

    // 1. Update the signature to accept lens info
    fun startRecording(cameraSelector: CameraSelector, isFront: Boolean, onVideoSaved: (String) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture)

                // 2. Use your helper to generate the filename with the side tag
                val fileName = generateFilename(isFront)
                val file = File(getExternalFilesDir(null), fileName)

                val outputOptions = FileOutputOptions.Builder(file).build()

                recording = videoCapture?.output
                    ?.prepareRecording(this, outputOptions)
                    ?.withAudioEnabled()
                    ?.start(ContextCompat.getMainExecutor(this)) { event ->
                        // 3. Keep the logic inside the listener where 'event' is valid
                        if (event is VideoRecordEvent.Finalize) {
                            val path = file.absolutePath
                            if (!event.hasError()) {
                                updateNotification("Recording saved")
                                onVideoSaved(path)
                            } else {
                                Log.e("CamService", "Video error: ${event.error}")
                                updateNotification("Recording failed")
                            }
                        }
                    }

                updateNotification("Recording active...")
            } catch (e: Exception) {
                Log.e("CamService", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    fun stopRecording() {
        recording?.stop()
        recording = null
        updateNotification("Recording saved")
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "camera_channel")
            .setContentTitle("Camera Recorder")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, createNotification(content))
    }
    // Pass the lens facing info to the service when starting a record
    private fun generateFilename(isFront: Boolean): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val side = if (isFront) "front" else "back"
        return "REC_${side}_${timestamp}.mp4"
    }

    // Remove the logic that refers to 'event' here
    private fun onVideoSaved(path: String) {
        Log.d("CamService", "Video saved to: $path")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("camera_channel", "Recording Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}