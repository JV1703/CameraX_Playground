package com.example.intermediatesubmission.presentation.ui.fragment.upload

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.intermediatesubmission.databinding.FragmentPictureBinding

class PictureFragment : BaseUploadFragment() {

    private var _binding: FragmentPictureBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imageView.apply {
            load(viewModel.picture!!)
            setOnClickListener {
                val extra = FragmentNavigatorExtras(binding.imageView to "image")
                val action = PictureFragmentDirections.actionPictureFragmentToAddStoryFragment()
                findNavController().navigate(
                    action.actionId,
                    null,
                    null,
                    extra
                )
            }
        }

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}