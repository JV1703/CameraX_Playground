package com.example.intermediatesubmission.presentation.ui.adapters.story

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState.Loading.endOfPaginationReached
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.data.local.entity.EntityStoryRemoteKey
import com.example.intermediatesubmission.data.network.model.stories.asEntityStory
import com.example.intermediatesubmission.data.repository.StoryRepository
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(private val storyRepository: StoryRepository) :
    RemoteMediator<Int, EntityStory>() {

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, EntityStory>
    ): MediatorResult {
        Log.i("mediator", "load function is called")

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with `endOfPaginationReached = false` because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                // the end of pagination for prepend.
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with `endOfPaginationReached = false` because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }

        try {
            val response = storyRepository.getAllStories(page, state.config.pageSize)

            if (response.isSuccessful) {
                response.body()?.let {
                    val stories = it.listNetworkStory
//                    val endOfPagination = stories.isEmpty()
                    val endOfPagination = stories.size < state.config.pageSize
                    val prev = if (page == 1) null else page - 1
                    val next = if (endOfPagination) null else page + 1

                    // reset room tables
                    if (loadType == LoadType.REFRESH) {
                        storyRepository.clearStories()
                        storyRepository.clearRemoteKeys()
                    }

                    // insert story remote keys to ROOM
                    if (it.listNetworkStory.isNotEmpty()) {
                        val keys = it.listNetworkStory.map {
                            EntityStoryRemoteKey(
                                id = it.id, prevKey = prev, nextKey = next
                            )
                        }

                        storyRepository.insertAllRemoteKeys(keys)
                    }

                    // insert stories to ROOM
                    storyRepository.insertAllStories(it.listNetworkStory.map { story ->
                        story.asEntityStory()
                    })

                    Log.i(
                        "mediator",
                        "details - current page: $page, prev page: $prev, next page: $next"
                    )
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            Log.i("mediator", "load: Error - IO Exception")
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Log.i("mediator", "load: Error - HTTP Exception")
            return MediatorResult.Error(exception)
        } catch (exception: Exception) {
            Log.i("mediator", "load: Error - Other Exception")
            return MediatorResult.Error(exception)
        }

    }


    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EntityStory>): EntityStoryRemoteKey? {
        return state.anchorPosition?.let {
            state.closestItemToPosition(it)?.let { story ->
                storyRepository.remoteKeyStoryId(story.id)
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EntityStory>): EntityStoryRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { story ->
            storyRepository.remoteKeyStoryId(story.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EntityStory>): EntityStoryRemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { story ->
            // Get the remote keys of the first items retrieved
            storyRepository.remoteKeyStoryId(story.id)
        }
    }
}