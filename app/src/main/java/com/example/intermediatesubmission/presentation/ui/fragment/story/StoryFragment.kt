package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.dateFormatter
import com.example.intermediatesubmission.databinding.FragmentStoryBinding
import com.example.intermediatesubmission.presentation.ui.model.Story
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoryFragment : BaseStoryFragment() {

    private var _binding: FragmentStoryBinding? = null
    private val binding get() = _binding!!

//    private val navArgs: StoryFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enterAnimation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.fade
        )
        val exitAnimation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.explode
        )

        sharedElementEnterTransition = enterAnimation
        sharedElementReturnTransition = exitAnimation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun bind(story: Story) {
        binding.ivDetailPhoto.load(story.photoUrl) {
            placeholder(R.drawable.loading_animation)
            crossfade(600)
            error(R.drawable.ic_error_placeholder)
        }

        binding.tvDetailName.text = story.name
        binding.storyTimestamp.text = story.createdAt.dateFormatter()
        binding.tvDetailDescription.text = story.description
    }

}