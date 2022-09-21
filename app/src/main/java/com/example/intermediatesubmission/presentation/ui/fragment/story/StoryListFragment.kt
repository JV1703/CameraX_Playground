package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.hasInternetConnection
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentStoryListBinding
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryLoadStateAdapter
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoryListFragment : BaseStoryFragment() {

    private var _binding: FragmentStoryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuHost: MenuHost
    private lateinit var storyListAdapter: StoryPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.i("StoryListFragment", "onCreateView")
        _binding = FragmentStoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("StoryListFragment", "onViewCreated")
        postponeEnterTransition()
        setupMenu()
        setupAdapter()

        viewModel.stories.observe(viewLifecycleOwner) { pagingData ->
            lifecycleScope.launch {
                storyListAdapter.submitData(pagingData)
            }
            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        viewModel.page.observe(viewLifecycleOwner) {
            binding.storyRv.scrollToPosition(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            storyListAdapter.loadStateFlow.collect { loadState ->
                Log.i("mediator", "loadState: $loadState")
                binding.refresh.setOnRefreshListener {
                    if (hasInternetConnection(requireContext())) {
                        storyListAdapter.refresh()
                        binding.storyRv.scrollToPosition(0)
                    } else {
                        makeToast(getString(R.string.fail_refresh))
                    }

                    binding.refresh.isRefreshing = loadState.source.refresh is LoadState.Loading
                }

                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && storyListAdapter.itemCount == 0

                binding.errorMsg.isVisible = isListEmpty
                binding.errorMsg.text = getString(R.string.no_result)
                binding.storyRv.isVisible = !isListEmpty

                binding.progressInd.isVisible = loadState.source.refresh is LoadState.Loading
                binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error ?: loadState.prepend as? LoadState.Error
                errorState?.let {
                    makeToast("\uD83D\uDE28 Wooops ${it.error.localizedMessage}")
                }

            }
        }

        binding.fab.setOnClickListener {
            val action = StoryListFragmentDirections.actionStoryListFragmentToUploadActivity()
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        Log.i("StoryListFragment", "onDestroy")
        _binding = null
        super.onDestroy()
    }

    private fun setupAdapter() {
        storyListAdapter = StoryPagingAdapter()
        binding.storyRv.adapter =
            storyListAdapter.withLoadStateHeaderAndFooter(header = StoryLoadStateAdapter { storyListAdapter.retry() },
                footer = StoryLoadStateAdapter { storyListAdapter.retry() })
        binding.storyRv.setHasFixedSize(true)
        binding.storyRv.itemAnimator = null
    }

    private fun setupMenu() {
        menuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.log_out_menu -> {
                        val action =
                            StoryListFragmentDirections.actionStoryListFragmentToAuthActivity()
                        findNavController().navigate(action)
                        viewModel.logout()
                        activity?.finish()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}