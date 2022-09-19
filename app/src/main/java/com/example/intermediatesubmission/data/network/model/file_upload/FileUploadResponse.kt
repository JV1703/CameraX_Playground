package com.example.intermediatesubmission.data.network.model.file_upload


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    @Json(name = "error")
    val error: Boolean,
    @Json(name = "message")
    val message: String
)