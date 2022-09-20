package com.example.intermediatesubmission.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class ImageFile(
    val image: File
) : Parcelable