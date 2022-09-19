package com.example.intermediatesubmission.data.network.model.stories


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoriesResponse(
    @Json(name = "error")
    val error: Boolean,
    @Json(name = "listStory")
    val listNetworkStory: List<NetworkStory>,
    @Json(name = "message")
    val message: String
)