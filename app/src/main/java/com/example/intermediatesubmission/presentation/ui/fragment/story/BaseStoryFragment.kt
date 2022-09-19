package com.example.intermediatesubmission.presentation.ui.fragment.story

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

open class BaseStoryFragment : Fragment() {

    protected val viewModel: StoryViewModel by activityViewModels()

}