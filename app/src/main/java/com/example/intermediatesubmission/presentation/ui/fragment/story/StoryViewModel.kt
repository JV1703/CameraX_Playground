package com.example.intermediatesubmission.presentation.ui.fragment.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.intermediatesubmission.data.repository.StoryRepository
import com.example.intermediatesubmission.presentation.ui.adapters.story.StoryRemoteMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    @OptIn(ExperimentalPagingApi::class)
    private val _stories = Pager(
        config = PagingConfig(pageSize = 10),
        remoteMediator = StoryRemoteMediator(storyRepository),
    ) {
        storyRepository.getAllStories()
    }.liveData.cachedIn(viewModelScope)
    val stories get() = _stories

    private val _page = MutableLiveData<Int>()
    val page: LiveData<Int> get() = _page

    fun savePage(page: Int) {
        _page.value = page
    }

    fun logout() {
        viewModelScope.launch {
            try {
                storyRepository.removeAuthTokenFromPreferencesStore()
            } catch (e: Exception) {
                Log.e("StoryViewModel", "logout() - ${e.localizedMessage}")
            }
        }
    }

    override fun onCleared() {
        Log.i("StoryViewModel", "onCleared: called")
        super.onCleared()
    }

}