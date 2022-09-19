package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.makeToast
import com.example.intermediatesubmission.databinding.FragmentStoryListBinding
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryLoadStateAdapter
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoryListFragment : BaseStoryFragment() {

    private var _binding: FragmentStoryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuHost: MenuHost
    private lateinit var storyListAdapter: StoryPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("storyListFragment", "onCreate - is called")
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        Log.i("storyListFragment", "onStart - is called")
        super.onStart()
    }

    override fun onResume() {
        storyListAdapter.refresh()
        super.onResume()
        Log.i("storyListFragment", "onResume - is called")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.i("storyListFragment", "onViewStateRestored - is called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onPause() {
        Log.i("storyListFragment", "onPause - is called")
        super.onPause()
    }

    override fun onStop() {
        Log.i("storyListFragment", "onStop - is called")
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.i("storyListFragment", "onSaveInstanceState - is called")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        Log.i("storyListFragment", "onDestroyView - is called")
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.i("storyListFragment", "onCreateView - is called")
        _binding = FragmentStoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("storyListFragment", "onViewCreated - is called")
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stories.collectLatest { pagingData ->
                    storyListAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            storyListAdapter.loadStateFlow.collect { loadState ->
                Log.i("mediator", "loadState: ${loadState}")
                binding.refresh.setOnRefreshListener {
                    if (viewModel.hasInternetConnection()) {
                        storyListAdapter.refresh()
                        binding.storyRv.scrollToPosition(0)
                        binding.refresh.isRefreshing = false
                    } else {
                        binding.refresh.isRefreshing = false
                        makeToast("unable to refresh, please make sure connection is available")
                    }
                }

                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && storyListAdapter.itemCount == 0

                binding.errorMsg.isVisible = isListEmpty
                binding.errorMsg.text = requireContext().getString(R.string.no_result)
                binding.storyRv.isVisible = !isListEmpty

                binding.progressInd.isVisible = loadState.source.refresh is LoadState.Loading
                binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error ?: loadState.prepend as? LoadState.Error
                errorState?.let {
                    makeToast("\uD83D\uDE28 Wooops ${it.error}")
                }

            }
        }

        binding.fab.setOnClickListener {
            val action = StoryListFragmentDirections.actionStoryListFragmentToAddStoryFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        Log.i("storyListFragment", "onDestroy - is called")
        _binding = null
        super.onDestroy()
    }

    private fun setupAdapter() {
        storyListAdapter = StoryPagingAdapter()
        binding.storyRv.adapter =
            storyListAdapter.withLoadStateHeaderAndFooter(header = StoryLoadStateAdapter { storyListAdapter.retry() },
                footer = StoryLoadStateAdapter { storyListAdapter.retry() })
        binding.storyRv.setHasFixedSize(true)
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