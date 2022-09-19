package com.example.intermediatesubmission.data.repository

import com.example.intermediatesubmission.data.data_source.DicodingLocalDataSource
import com.example.intermediatesubmission.data.data_source.DicodingRemoteDataSource
import com.example.intermediatesubmission.data.network.model.login.LoginResponse
import com.example.intermediatesubmission.data.network.model.register.RegisterResponse
import com.example.intermediatesubmission.data.network.model.stories.StoriesResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val remote: DicodingRemoteDataSource,
    private val local: DicodingLocalDataSource
) {
    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Response<RegisterResponse> =
        remote.registerUser(name, email, password)

    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> =
        remote.login(email, password)

    suspend fun saveAuthTokenToPreferencesStore(authToken: String) {
        local.saveAuthTokenToPreferencesStore(authToken)
    }

    val getAuthTokenFromPreferencesStore: Flow<String> = local.getAuthTokenFromPreferencesStore()

    suspend fun getAllStories(
        page: Int,
        size: Int,
        location: Int? = 0
    ): Response<StoriesResponse> = remote.getAllStories(page, size, location)

}