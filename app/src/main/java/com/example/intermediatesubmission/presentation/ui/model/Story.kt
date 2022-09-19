package com.example.intermediatesubmission.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    val createdAt: String,
    val description: String,
    val id: String,
    val name: String,
    val photoUrl: String
) : Parcelable