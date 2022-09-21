package com.example.intermediatesubmission.presentation.ui.adapters.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.intermediatesubmission.R
import com.example.intermediatesubmission.common.Constants.CROSS_FADE_DURATION
import com.example.intermediatesubmission.common.dateFormatter
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.databinding.StoryVpVhBinding

class StoryViewPagerAdapter :
    PagingDataAdapter<EntityStory, StoryViewPagerAdapter.StoryVpVh>(StoryPagingAdapter) {

    class StoryVpVh(private val binding: StoryVpVhBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentStory: EntityStory) {
            binding.apply {
                ViewCompat.setTransitionName(storyIv, "story_iv_details")
                ViewCompat.setTransitionName(storyAuthor, "story_author_details")
                ViewCompat.setTransitionName(storyTimestamp, "story_timestamp_details")
                ViewCompat.setTransitionName(storyDescription, "story_description_details")

                storyIv.load(currentStory.photoUrl) {
                    placeholder(R.drawable.loading_animation)
                    crossfade(CROSS_FADE_DURATION)
                    error(R.drawable.ic_error_placeholder)
                }

                storyAuthor.text = currentStory.name
                storyTimestamp.text = currentStory.createdAt.dateFormatter()
                storyDescription.text = currentStory.description
            }
        }
    }

    override fun onBindViewHolder(holder: StoryVpVh, position: Int) {
        val currentStory = getItem(position)
        currentStory?.let {
            holder.bind(currentStory)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryVpVh {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StoryVpVh(StoryVpVhBinding.inflate(layoutInflater, parent, false))
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