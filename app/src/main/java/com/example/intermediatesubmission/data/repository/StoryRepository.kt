package com.example.intermediatesubmission.data.repository

import androidx.paging.PagingSource
import com.example.intermediatesubmission.data.data_source.DicodingLocalDataSource
import com.example.intermediatesubmission.data.data_source.DicodingRemoteDataSource
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.data.local.entity.EntityStoryRemoteKey
import com.example.intermediatesubmission.data.network.model.stories.StoriesResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val remote: DicodingRemoteDataSource,
    private val local: DicodingLocalDataSource
) {

    suspend fun getAllStories(
        page: Int,
        size: Int,
        location: Int? = 0
    ): Response<StoriesResponse> =
        remote.getAllStories(page, size, location)

    // Story Dao
    suspend fun insertAllStories(stories: List<EntityStory>) = local.insertAllStories(stories)

    fun getAllStories(): PagingSource<Int, EntityStory> = local.getAllStories()

    suspend fun clearStories() = local.clearStories()

    // Story Remote Key Dao
    suspend fun insertAllRemoteKeys(remoteKeys: List<EntityStoryRemoteKey>) =
        local.insertAllRemoteKeys(remoteKeys)

    suspend fun remoteKeyStoryId(id: String): EntityStoryRemoteKey? = local.remoteKeyStoryId(id)

    suspend fun clearRemoteKeys() = local.clearRemoteKeys()

    suspend fun uploadFile(
        file: MultipartBody.Part,
        description: RequestBody
    ) = remote.uploadFile(file, description)

    suspend fun removeAuthTokenFromPreferencesStore() {
        local.removeAuthTokenFromPreferencesStore()
    }

    fun getAllStoriesFromDb(): Flow<List<EntityStory>> = local.getAllStoriesFromDb()

}