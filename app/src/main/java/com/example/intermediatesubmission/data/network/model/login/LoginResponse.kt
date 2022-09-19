package com.example.intermediatesubmission.data.network.model.login


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "error")
    val error: Boolean,
    @Json(name = "loginResult")
    val loginResult: LoginResult,
    @Json(name = "message")
    val message: String
)