package com.example.intermediatesubmission.data.network.model

import com.example.intermediatesubmission.data.network.model.file_upload.FileUploadResponse
import com.example.intermediatesubmission.data.network.model.login.LoginResponse
import com.example.intermediatesubmission.data.network.model.register.RegisterResponse
import com.example.intermediatesubmission.data.network.model.stories.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


interface DicodingApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("stories")
    suspend fun getAllStories(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int? = 0
    ): Response<StoriesResponse>

    @Multipart
    @POST("stories")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Response<FileUploadResponse>

}