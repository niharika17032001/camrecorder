package com.example.camrecorder

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camrecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var service: CameraForegroundService? = null
    private var isRecording = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as CameraForegroundService.LocalBinder).getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) { service = null }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startServiceAndBind()
            startCameraPreview()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10)
        }

        binding.btnRecord.setOnClickListener {
            if (isRecording) stopAction() else startAction()
        }

        binding.btnSwitch.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

            if (isRecording) {
                stopAction()
                startAction()
            }
            startCameraPreview()
        }
    }

    // In MainActivity.kt inside startAction()
    private fun startAction() {
        val isFront = lensFacing == CameraSelector.LENS_FACING_FRONT
        service?.startRecording(
            CameraSelector.Builder().requireLensFacing(lensFacing).build(),
            isFront // Pass the lens info here
        ) { path ->
            runOnUiThread { Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show() }
        }
        isRecording = true
        binding.btnRecord.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun stopAction() {
        service?.stopRecording()
        isRecording = false
        binding.btnRecord.setImageResource(android.R.drawable.ic_menu_camera)
    }

    private fun startCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview)
            } catch (e: Exception) { }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startServiceAndBind() {
        val intent = Intent(this, CameraForegroundService::class.java)
        startService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}