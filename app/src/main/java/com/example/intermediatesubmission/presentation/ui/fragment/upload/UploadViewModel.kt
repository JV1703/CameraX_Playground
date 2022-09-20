package com.example.intermediatesubmission.presentation.ui.fragment.upload

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.MyApplication
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.networkResultHandler
import com.example.intermediatesubmission.common.reduceFileImage
import com.example.intermediatesubmission.data.network.model.file_upload.FileUploadResponse
import com.example.intermediatesubmission.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val storyRepository: StoryRepository, private val application: MyApplication
) : ViewModel() {

    private val _uploadResponse = MutableLiveData<NetworkResult<FileUploadResponse>>()
    val uploadResponse: LiveData<NetworkResult<FileUploadResponse>> get() = _uploadResponse

    private var _picture: File? = null
    val picture get() = _picture

    private var _description: String = ""
    val description get() = _description

    fun savePicture(file: File?) {
        _picture = file
    }

    fun saveDescription(description: String) {
        _description = description
    }

    private var _cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val cameraSelector get() = _cameraSelector

    fun setCamera() {
        _cameraSelector =
            if (_cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
    }

    fun uploadFile(
        file: File, text: String
    ) {
        _uploadResponse.value = NetworkResult.Loading()

        val reducedImageFile = reduceFileImage(file)
        val requestImageFile = reducedImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val description = text.toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo", file.name, requestImageFile
        )

        viewModelScope.launch {
            try {
                val response = storyRepository.uploadFile(imageMultipart, description)
                _uploadResponse.value = networkResultHandler(response)
            } catch (e: IOException) {
                Log.e("AuthViewModel", "IOException")
            } catch (e: HttpException) {
                Log.e("AuthViewModel", "HTTPException - unexpected response")
            } catch (e: Exception) {
                _uploadResponse.value = NetworkResult.Error(e.message ?: "unexpected error")
            }
        }
    }
}

