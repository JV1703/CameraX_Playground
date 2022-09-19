package com.example.intermediatesubmission.data.network.model.stories


import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkStory(
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "lat")
    val lat: Double?,
    @Json(name = "lon")
    val lon: Double?,
    @Json(name = "name")
    val name: String,
    @Json(name = "photoUrl")
    val photoUrl: String
)

fun NetworkStory.asEntityStory() = EntityStory(
    createdAt = createdAt,
    description = description,
    id = id,
    lat = lat,
    lon = lon,
    name = name,
    photoUrl = photoUrl
)