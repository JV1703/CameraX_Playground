package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.*
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
                makeToast(getString(R.string.insert_picture))
            } else if (isEditTextEmpty()) {
                makeToast(getString(R.string.empty_description))
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
                    viewModel.setRefresh(true)
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
                    getString(R.string.permission_camera),
                    Snackbar.LENGTH_SHORT
                ).setAction(getString(R.string.close)) {}.show()
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
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
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

    private fun isEditTextEmpty(): Boolean {
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