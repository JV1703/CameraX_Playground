package com.example.intermediatesubmission.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.intermediatesubmission.data.local.entity.EntityStory
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStories(stories: List<EntityStory>)

    @Query("SELECT * FROM story_table")
    fun getAllStories(): PagingSource<Int, EntityStory>

    @Query("DELETE from story_table")
    suspend fun clearStories()

    @Query("SELECT * FROM story_table")
    fun getAll(): Flow<List<EntityStory>>

}