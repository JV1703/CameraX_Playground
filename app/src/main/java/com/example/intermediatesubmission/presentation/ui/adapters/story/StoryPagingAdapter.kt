package com.example.intermediatesubmission.presentation.ui.adapters.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.Constants.CROSS_FADE_DURATION
import com.example.intermediatesubmission.common.dateFormatter
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.databinding.StoryViewHolderBinding
import com.example.intermediatesubmission.presentation.ui.fragment.story.StoryListFragmentDirections

class StoryPagingAdapter :
    PagingDataAdapter<EntityStory, StoryPagingAdapter.StoryViewHolder>(DiffUtilCallback) {

    inner class StoryViewHolder(val binding: StoryViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentStory: EntityStory) {
            binding.apply {
                ViewCompat.setTransitionName(ivItemPhoto, "story_iv_details")
                ViewCompat.setTransitionName(tvItemName, "story_author_details")
                ViewCompat.setTransitionName(storyTimestamp, "story_timestamp_details")
                ViewCompat.setTransitionName(storyDescription, "story_description_details")

                ivItemPhoto.load(currentStory.photoUrl) {
                    placeholder(R.drawable.loading_animation)
                    crossfade(CROSS_FADE_DURATION)
                    error(R.drawable.ic_error_placeholder)
                }

                tvItemName.text = currentStory.name
                storyTimestamp.text = currentStory.createdAt.dateFormatter()
                storyDescription.text = currentStory.description

            }

        }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val currentStory = getItem(position)
        currentStory?.let {
            holder.bind(currentStory)
        }
        holder.itemView.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                holder.binding.ivItemPhoto to "story_iv_details",
                holder.binding.tvItemName to "story_author_details",
                holder.binding.storyTimestamp to "story_timestamp_details",
                holder.binding.storyDescription to "story_description_details"
            )

            val action =
                StoryListFragmentDirections.actionStoryListFragmentToStoryViewPagerFragment(position = position)
            holder.itemView.findNavController().navigate(
                action.actionId,
                action.arguments,
                null,
                extras
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StoryViewHolder(
            StoryViewHolderBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    companion object DiffUtilCallback : DiffUtil.ItemCallback<EntityStory>() {
        override fun areItemsTheSame(oldItem: EntityStory, newItem: EntityStory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EntityStory, newItem: EntityStory): Boolean {
            return oldItem == newItem
        }
    }
}