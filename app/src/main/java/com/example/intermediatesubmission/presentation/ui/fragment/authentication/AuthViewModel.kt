package com.example.intermediatesubmission.presentation.ui.fragment.authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.example.MyApplication
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.networkResultHandler
import com.example.intermediatesubmission.data.network.model.login.LoginResponse
import com.example.intermediatesubmission.data.network.model.register.RegisterResponse
import com.example.intermediatesubmission.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository, private val application: MyApplication
) : ViewModel() {

    private val _registerResponse = MutableLiveData<NetworkResult<RegisterResponse>>()
    val registerResponse: LiveData<NetworkResult<RegisterResponse>> get() = _registerResponse

    private val _loginResponse = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginResponse: LiveData<NetworkResult<LoginResponse>> get() = _loginResponse

    val authToken = authRepo.getAuthTokenFromPreferencesStore.asLiveData()

    fun registerUser(
        name: String, email: String, password: String
    ) {
        _registerResponse.value = NetworkResult.Loading()

        if (hasInternetConnection()) {
            viewModelScope.launch {
                try {
                    val response = authRepo.registerUser(name, email, password)
                    _registerResponse.value = networkResultHandler(response)
                } catch (e: IOException) {
                    Log.e("AuthViewModel", "IOException")
                } catch (e: HttpException) {
                    Log.e("AuthViewModel", "HTTPException - unexpected response")
                } catch (e: Exception) {
                    _registerResponse.value = NetworkResult.Error(e.message ?: "unexpected error")
                }
            }
        } else {
            _registerResponse.value = NetworkResult.Error("No Internet Connection")
        }
    }

    fun login(
        email: String, password: String
    ) {
        _loginResponse.value = NetworkResult.Loading()

        viewModelScope.launch {
            if (hasInternetConnection()) {
                try {
                    val response = authRepo.login(email, password)
                    _loginResponse.value = networkResultHandler(response)
                    authRepo.saveAuthTokenToPreferencesStore(response.body()?.loginResult?.token!!)
                } catch (e: IOException) {
                    Log.e("AuthViewModel", "IOException")
                } catch (e: HttpException) {
                    Log.e("AuthViewModel", "HTTPException - unexpected response")
                } catch (e: Exception) {
                    _registerResponse.value = NetworkResult.Error(e.message ?: "unexpected error")
                }
            } else {
                _loginResponse.value = NetworkResult.Error("No Internet Connection")
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
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

}