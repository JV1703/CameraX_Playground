package com.example.intermediatesubmission.presentation.ui.fragment.authentication

import android.util.Log
import androidx.lifecycle.*
import com.example.MyApplication
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.NetworkResult
import com.example.intermediatesubmission.common.hasInternetConnection
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

        if (hasInternetConnection(application)) {
            viewModelScope.launch {
                try {
                    val response = authRepo.registerUser(name, email, password)
                    _registerResponse.value = networkResultHandler(response)
                } catch (e: IOException) {
                    Log.e("AuthViewModel", "IOException")
                } catch (e: HttpException) {
                    Log.e("AuthViewModel", "HTTPException - unexpected response")
                } catch (e: Exception) {
                    _registerResponse.value =
                        NetworkResult.Error(e.localizedMessage ?: "unexpected error")
                }
            }
        } else {
            _registerResponse.value =
                NetworkResult.Error(application.getString(R.string.no_internet))
        }
    }

    fun login(
        email: String, password: String
    ) {
        _loginResponse.value = NetworkResult.Loading()

        viewModelScope.launch {
            if (hasInternetConnection(application)) {
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
                _loginResponse.value =
                    NetworkResult.Error(application.getString(R.string.no_internet))
            }
        }
    }

}