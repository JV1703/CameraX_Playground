package com.example.intermediatesubmission.presentation.ui.fragment.upload

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.hasInternetConnection
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.common.uriToFile
import com.example.intermediatesubmission.databinding.FragmentAddStoryBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddStoryFragment : BaseUploadFragment() {

    companion object {
        private val REQUIRED_CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.edAddDescription.setText(viewModel.description, TextView.BufferType.EDITABLE)
        loadPicture(viewModel.picture)

        binding.cameraBtn.setOnClickListener {
            requestPermission()
        }

        binding.galleryBtn.setOnClickListener {
            startGallery()
        }

        binding.buttonAdd.setOnClickListener {
            if (hasInternetConnection(requireContext())) {
                if (viewModel.picture == null) {
                    makeToast(getString(R.string.empty_picture))
                } else if (it == null) {
                    makeToast(getString(R.string.empty_description))
                } else {
                    uploadFile(viewModel.picture!!, binding.edAddDescription.text.toString())
                }
            } else {
                makeToast(getString(R.string.no_internet))
            }
        }

        viewModel.uploadResponse.observe(viewLifecycleOwner) { networkResult ->
            when (networkResult) {
                is NetworkResult.Loading -> {
                    isLoading(true)
                }

                is NetworkResult.Success -> {
                    isLoading(false)
                    activity?.finish()
                }

                is NetworkResult.Error -> {
                    isLoading(false)
                    makeToast(networkResult.message ?: "unexpected error")
                }
            }
        }
    }


    override fun onDestroy() {
        saveDescription(binding.edAddDescription.text.toString())
        _binding = null
        super.onDestroy()
    }

    private fun saveDescription(description: String) {
        val trimmedDescription = description.trim()
        if (trimmedDescription.isNotEmpty()) {
            viewModel.saveDescription(trimmedDescription)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val action = AddStoryFragmentDirections.actionAddStoryFragmentToCameraFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(
                    binding.root, getString(R.string.permission_camera), Snackbar.LENGTH_SHORT
                ).setAction(getString(R.string.close)) {}.show()
            }
        }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, requireContext())
            viewModel.savePicture(myFile)
            loadPicture(viewModel.picture)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun hasCameraPermission() = REQUIRED_CAMERA_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLoading(isLoading: Boolean) {

        binding.buttonAdd.isEnabled = isLoading
        binding.cameraBtn.isEnabled = isLoading
        binding.galleryBtn.isEnabled = isLoading
        binding.previewImageView.isEnabled = isLoading
        if (isLoading) {
            binding.progressInd.visibility = View.VISIBLE
        } else {
            binding.progressInd.visibility = View.GONE
        }
    }

    private fun isEditTextEmpty(): Boolean {
        return binding.edAddDescription.text.isNullOrEmpty()
    }

    private fun uploadFile(file: File, text: String) {
        viewModel.uploadFile(file, text)
    }

    private fun loadPicture(picture: File?) {
        if (picture != null) {
            binding.previewImageView.load(picture)
            binding.previewImageView.isEnabled = true
            binding.previewImageView.setOnClickListener {
                val extra = FragmentNavigatorExtras(binding.previewImageView to "image")
                val action = AddStoryFragmentDirections.actionAddStoryFragmentToPictureFragment()
                findNavController().navigate(
                    action.actionId, null, null, extra
                )
            }

        } else {
            binding.previewImageView.load(R.drawable.ic_error_placeholder)
            binding.previewImageView.isEnabled = false
        }
    }

    private fun requestPermission() {
        when {
            hasCameraPermission() -> {
                val action = AddStoryFragmentDirections.actionAddStoryFragmentToCameraFragment()
                findNavController().navigate(action)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.permission_camera))
                    .setMessage(getString(R.string.permission_camera))
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