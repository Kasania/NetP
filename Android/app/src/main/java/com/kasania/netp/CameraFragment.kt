package com.kasania.netp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.camera_fragment.view.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class CameraFragment : Fragment() {
    private val REQUEST_CODE_CAMERA_PERMISSIONS = 10
    private val REQUEST_CODE_AUDIO_PERMISSIONS = 200
    private val REQUIRED_CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val REQUIRED_AUDIO_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var preview: Preview? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var audioExecutor: ExecutorService

    private val sampleRate = 44100
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    var minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private lateinit var recorder : AudioRecord

    private val enableVideo = AtomicBoolean(true)
    private val enableAudio = AtomicBoolean(true)
    private val recordingAudio = AtomicBoolean(true)

    private val blankBitmap = Bitmap.createBitmap(240,360, Bitmap.Config.ARGB_8888)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.camera_fragment, container, false)

        Connection.instance.onSyncCanceled { requireActivity().supportFragmentManager.beginTransaction().replace(R.id.content_root, LoginFragment()).commit() }

        rootView.video_toggle_button.setOnClickListener {
            if(enableVideo.get()){
                enableVideo.set(false)
            }
            else{
                enableVideo.set(true)
            }
        }

        rootView.audio_toggle_button.setOnClickListener {
            if(enableAudio.get()){
                enableAudio.set(false)
            }
            else{
                enableAudio.set(true)
            }
        }

        if (allPermissionsGranted()) {

            startCamera()

            cameraExecutor = Executors.newSingleThreadExecutor()
            audioExecutor = Executors.newSingleThreadExecutor()
            audioExecutor.execute(this::startAudioRecording)

        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(), REQUIRED_CAMERA_PERMISSIONS, REQUEST_CODE_CAMERA_PERMISSIONS
            )
            ActivityCompat.requestPermissions(
                    requireActivity(), REQUIRED_AUDIO_PERMISSIONS, REQUEST_CODE_AUDIO_PERMISSIONS
            )
        }

        return rootView
    }

    private fun startAudioRecording(){

        recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channelConfig, audioFormat, minBufSize)
        recorder.startRecording()

        Log.d("TAG", "startAudioRecording: $minBufSize")
        val buffer = ByteArray(minBufSize)
        while(recordingAudio.get()){
            if(enableAudio.get()){
                recorder.read(buffer, 0, buffer.size)
                Connection.instance.sendAudio(buffer)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                    .build()

            val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
            imageAnalysis.setAnalyzer(cameraExecutor, { image ->
                if(enableVideo.get()){
                    activity?.runOnUiThread {
                        try {
                            viewfinder.bitmap?.let { Connection.instance.sendImage(it) }
                        }catch (exp:java.lang.Exception){}
                    }
                }
                else{
                    Connection.instance.sendImage(blankBitmap)
                }
                image.close()
            })

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis
                )
                preview?.setSurfaceProvider(viewfinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e("CAMERA", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray
    ) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                        context,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun allPermissionsGranted() :Boolean {
        return (REQUIRED_CAMERA_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }
                and REQUIRED_AUDIO_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        enableVideo.set(false)
        enableAudio.set(false)
        recordingAudio.set(false)
        recorder.stop()

        cameraExecutor.shutdown()
        audioExecutor.shutdown()

    }

}