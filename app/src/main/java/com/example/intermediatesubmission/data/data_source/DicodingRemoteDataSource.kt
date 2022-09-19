package com.example.intermediatesubmission.data.data_source

import com.example.intermediatesubmission.data.network.model.DicodingApiService
import com.example.intermediatesubmission.data.network.model.login.LoginResponse
import com.example.intermediatesubmission.data.network.model.register.RegisterResponse
import com.example.intermediatesubmission.data.network.model.stories.StoriesResponse
import com.example.movies.common.di.CoroutinesQualifiers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class DicodingRemoteDataSource @Inject constructor(
    private val api: DicodingApiService,
    @CoroutinesQualifiers.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun registerUser(
        name: String,
        email: String,
        password: String
    ): Response<RegisterResponse> =
        withContext(ioDispatcher) {
            api.registerUser(name, email, password)
        }

    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> =
        withContext(ioDispatcher) {
            api.login(email, password)
        }

    suspend fun getAllStories(
        page: Int,
        size: Int,
        location: Int? = 0
    ): Response<StoriesResponse> =
        withContext(ioDispatcher) {
            api.getAllStories(page, size, location)
        }

    suspend fun uploadFile(
        file: MultipartBody.Part,
        description: RequestBody,
    ) = withContext(ioDispatcher) {
        api.uploadImage(file, description)
    }

}