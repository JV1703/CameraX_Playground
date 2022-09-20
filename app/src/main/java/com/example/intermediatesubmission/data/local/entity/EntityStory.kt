package com.example.intermediatesubmission.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.intermediatesubmission.data.model.Story

@Entity(tableName = "story_table")
data class EntityStory(
    val createdAt: String,
    val description: String,
    @PrimaryKey val id: String,
    val lat: Double?,
    val lon: Double?,
    val name: String,
    val photoUrl: String
)

fun EntityStory.asStory() = Story(
    createdAt = createdAt, description = description, id = id, name = name, photoUrl = photoUrl
)