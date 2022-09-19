package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.common.uriToFile
import com.example.intermediatesubmission.databinding.FragmentAddStoryBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class AddStoryFragment : BaseStoryFragment() {

    companion object {
        private val REQUIRED_CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!

    private var picture: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraBtn.setOnClickListener {
            requestPermission()
        }

        binding.galleryBtn.setOnClickListener {
            startGallery()
        }

        viewModel.file.observe(viewLifecycleOwner) { file ->

            picture = file

            binding.previewImageView.load(file)
            binding.previewImageView.updateLayoutParams { width = WRAP_CONTENT }
            binding.previewImageView.updateLayoutParams { height = WRAP_CONTENT }

        }

        binding.buttonAdd.setOnClickListener {
            if (picture == null) {
                makeToast("please insert picture")
            } else if (isEditTextEmpty()) {
                makeToast("There is no description")
            } else {
                uploadFile(picture!!, binding.edAddDescription.text.toString())
            }
        }

        viewModel.uploadResponse.observe(viewLifecycleOwner) { networkResult ->
            when (networkResult) {
                is NetworkResult.Loading -> {
                    isLoading(true)
                }

                is NetworkResult.Success -> {
                    isLoading(false)
                    val action =
                        AddStoryFragmentDirections.actionAddStoryFragmentToStoryListFragment()
                    findNavController().navigate(action)
                }

                is NetworkResult.Error -> {
                    makeToast(networkResult.message ?: "unexpected error")
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val action = AddStoryFragmentDirections.actionAddStoryFragmentToCameraFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(
                    binding.root,
                    "Camera permission is required to take photo",
                    Snackbar.LENGTH_SHORT
                ).setAction("Close") {}.show()
            }
        }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireContext())
            viewModel.saveFileToVm(myFile)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun hasCameraPermission() = REQUIRED_CAMERA_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressInd.visibility = View.VISIBLE
        } else {
            binding.progressInd.visibility = View.GONE
        }
    }

    private fun isImageViewEmpty(): Boolean {
        val imageView = binding.previewImageView
        val drawable = imageView.drawable
        var hasImage = drawable != null
        val test = imageView.drawable != ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_error_placeholder
        )

        if (hasImage && drawable is BitmapDrawable && test) {
            hasImage = drawable.bitmap != null
        }
        Log.i("isEmpty", "drawable: $drawable, test: ${test}, hasImage: $hasImage")
        return hasImage
    }

    private fun isEditTextEmpty(): Boolean {
        Log.i("isEmpty", "editText: ${binding.edAddDescription.text.isNullOrEmpty()}")
        return binding.edAddDescription.text.isNullOrEmpty()
    }

    private fun uploadFile(file: File, text: String) {
        viewModel.uploadFile(file, text)
    }

    private fun requestPermission() {
        when {
            hasCameraPermission() -> {
                val action = AddStoryFragmentDirections.actionAddStoryFragmentToCameraFragment()
                findNavController().navigate(action)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext()).setTitle("Camera permission required")
                    .setMessage("Camera permission is required to capture photo")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermission.launch(
                            Manifest.permission.CAMERA
                        )
                    }.setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.dismiss() }
                    .create().show()
            }

            else -> {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }
}