package com.example.intermediatesubmission.presentation.ui.fragment.upload

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.createFile
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : BaseUploadFragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private lateinit var orientationEventListener: OrientationEventListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCameraX()

        binding.switchCamera.setOnClickListener {
            selectCameraLens()
        }

        binding.captureImage.setOnClickListener {
            takePhoto()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun selectCameraLens() {
        viewModel.setCamera()
        startCameraX()
    }

    private fun startCameraX() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        imageCapture =
            ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

        orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            try {
                cameraProvider.unbindAll()
                bindPreview(cameraProvider)
            } catch (e: Exception) {
                Log.e("CameraFragment", "cameraX - error message: ${e.localizedMessage}")
                makeToast(getString(R.string.camera))
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(
            ContextCompat.getMainExecutor(requireContext()), binding.previewView.surfaceProvider
        )
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner, viewModel.cameraSelector, imageCapture, preview
        )
    }

    private fun takePhoto() {
        val imageCapture = imageCapture
        val metadata = ImageCapture.Metadata()
        metadata.isReversedHorizontal =
            viewModel.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        val photoFile = createFile(activity?.application!!)
        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build()
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    makeToast(getString(R.string.fail_picture))
                    Log.e(
                        "CameraFragment",
                        "cameraX - error code: ${exc.imageCaptureError}, error msg: ${exc.localizedMessage}"
                    )
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.savePicture(photoFile)
                    val action = CameraFragmentDirections.actionCameraFragmentToAddStoryFragment()
                    findNavController().navigate(action)
                }
            })
    }

    override fun onPause() {
        orientationEventListener.disable()
        super.onPause()
    }

}