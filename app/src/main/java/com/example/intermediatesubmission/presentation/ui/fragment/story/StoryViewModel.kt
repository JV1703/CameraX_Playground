package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.MyApplication
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.networkResultHandler
import com.example.intermediatesubmission.common.reduceFileImage
import com.example.intermediatesubmission.data.network.model.file_upload.FileUploadResponse
import com.example.intermediatesubmission.data.repository.StoryRepository
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryRemoteMediator
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
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository, private val application: MyApplication
) : ViewModel() {

    private val _file = MutableLiveData</*ImageHelper*/File>()
    val file: LiveData</*ImageHelper*/File> get() = _file

    private val _uploadResponse = MutableLiveData<NetworkResult<FileUploadResponse>>()
    val uploadResponse: LiveData<NetworkResult<FileUploadResponse>> get() = _uploadResponse

    private var _cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    val cameraSelector get() = _cameraSelector

    @OptIn(ExperimentalPagingApi::class)
    private val _stories = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = StoryRemoteMediator(storyRepository),
    ) {
        storyRepository.getAllStories()
    }.flow
    val stories get() = _stories

    fun saveFileToVm(file: File) {
        _file.value = file
    }

    fun setCamera() {
        _cameraSelector =
            if (_cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
    }

    fun hasInternetConnection(): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun uploadFile(
        file: File, text: String
    ) {
        _uploadResponse.value = NetworkResult.Loading()

        val reducedImageFile = reduceFileImage(file)
        val requestImageFile = reducedImageFile.asRequestBody("image/gif".toMediaTypeOrNull())
        val description = text.toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo", file.name, requestImageFile
        )

        if (hasInternetConnection()) {
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
        } else {
            _uploadResponse.value = NetworkResult.Error("No Internet Connection")
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                storyRepository.removeAuthTokenFromPreferencesStore()
            } catch (e: Exception) {
                Log.e("StoryViewModel", "logout() - ${e.message}")
            }
        }
    }

    override fun onCleared() {
        Log.i("StoryViewModel", "onCleared: called")
        super.onCleared()
    }

}