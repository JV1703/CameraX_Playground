package com.example.intermediatesubmission.data.data_source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.PagingSource
import com.example.intermediatesubmission.data.data_source.PreferencesKeys.AUTH_TOKEN
import com.example.intermediatesubmission.data.local.dao.StoryDao
import com.example.intermediatesubmission.data.local.dao.StoryRemoteKeyDao
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.data.local.entity.EntityStoryRemoteKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

const val AUTH_PREFERENCES_NAME = "auth_preferences"

object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
}

class DicodingLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val storyDao: StoryDao,
    private val storyRemoteKeyDao: StoryRemoteKeyDao
) {

    suspend fun saveAuthTokenToPreferencesStore(authToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = authToken
        }
    }

    fun getAuthTokenFromPreferencesStore(): Flow<String> =
        dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[AUTH_TOKEN] ?: ""
        }

    suspend fun removeAuthTokenFromPreferencesStore() {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = ""
        }
    }

    // Story Dao
    suspend fun insertAllStories(stories: List<EntityStory>) = storyDao.insertAllStories(stories)

    fun getAllStories(): PagingSource<Int, EntityStory> = storyDao.getAllStories()

    suspend fun clearStories() = storyDao.clearStories()

    // Story Remote Key Dao
    suspend fun insertAllRemoteKeys(remoteKeys: List<EntityStoryRemoteKey>) =
        storyRemoteKeyDao.insertAllRemoteKeys(remoteKeys)

    suspend fun remoteKeyStoryId(id: String): EntityStoryRemoteKey? =
        storyRemoteKeyDao.remoteKeyStoryId(id)

    suspend fun clearRemoteKeys() = storyRemoteKeyDao.clearRemoteKeys()

    // etc
    fun getAllStoriesFromDb(): Flow<List<EntityStory>> = storyDao.getAll()
}