package com.example.intermediatesubmission.data.network.model.register


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "error")
    val error: Boolean,
    @Json(name = "message")
    val message: String
)