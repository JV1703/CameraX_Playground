package com.example.intermediatesubmission.presentation.ui.adapters.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.intermediatesubmission.databinding.StoryLoadStateViewHolderBinding

class StoryLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<StoryLoadStateAdapter.StoryLoadStateViewHolder>() {

    class StoryLoadStateViewHolder(
        private val binding: StoryLoadStateViewHolderBinding,
        private val retry: () -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorMsg.isVisible = loadState is LoadState.Error
            binding.retryButton.setOnClickListener { retry.invoke() }
        }

    }

    override fun onBindViewHolder(holder: StoryLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): StoryLoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StoryLoadStateViewHolder(
            StoryLoadStateViewHolderBinding.inflate(
                layoutInflater,
                parent,
                false
            ), retry
        )
    }
}