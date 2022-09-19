package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.intermediatesubmission.databinding.FragmentStoryViewPagerBinding
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoryViewPagerFragment : BaseStoryFragment() {

    private var _binding: FragmentStoryViewPagerBinding? = null
    private val binding get() = _binding!!

    private val navArgs: StoryViewPagerFragmentArgs by navArgs()

    private lateinit var storyViewPagerAdapter: StoryViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enterAnimation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.slide_right
        )

        sharedElementEnterTransition = enterAnimation
        sharedElementReturnTransition = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        setupViewPager()

        binding.scrollView.isFillViewport = true

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stories.collectLatest { pagingData ->
                    storyViewPagerAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            storyViewPagerAdapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && storyViewPagerAdapter.itemCount == 0

                if (!isListEmpty) {
                    binding.viewPager.setCurrentItem(navArgs.position, false)
                    (view.parent as? ViewGroup)?.viewTreeObserver?.addOnPreDrawListener {
                        startPostponedEnterTransition()
                        true
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setupViewPager() {
        storyViewPagerAdapter = StoryViewPagerAdapter()
        binding.viewPager.adapter = storyViewPagerAdapter
//        binding.viewPager.reduceViewPagerSensitivity(15)
    }

    private fun ViewPager2.reduceViewPagerSensitivity(n: Int) {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * n)
    }
}