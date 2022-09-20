package com.example.intermediatesubmission.presentation.ui.fragment.upload

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseUploadFragment : Fragment() {

    val viewModel: UploadViewModel by activityViewModels()

}