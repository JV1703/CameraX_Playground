package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.MyApplication
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.data.repository.StoryRepository
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryRemoteMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository, private val application: MyApplication
) : ViewModel() {

    @OptIn(ExperimentalPagingApi::class)
    private val _stories = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = StoryRemoteMediator(storyRepository),
    ) {
        storyRepository.getAllStories()
    }.flow
    val stories get() = _stories

    private val _storiesDb = storyRepository.getAllStoriesFromDb().asLiveData()
    val storiesDb: LiveData<List<EntityStory>> get() = _storiesDb

    fun logout() {
        viewModelScope.launch {
            try {
                storyRepository.removeAuthTokenFromPreferencesStore()
            } catch (e: Exception) {
                Log.e("StoryViewModel", "logout() - ${e.message}")
            }
        }
    }

    override fun onCleared() {
        Log.i("StoryViewModel", "onCleared: called")
        super.onCleared()
    }

}