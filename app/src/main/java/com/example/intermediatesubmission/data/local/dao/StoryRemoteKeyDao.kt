package com.example.intermediatesubmission.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.intermediatesubmission.data.local.entity.EntityStoryRemoteKey

@Dao
interface StoryRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(remoteKeys: List<EntityStoryRemoteKey>)

    @Query("SELECT * FROM story_remote_key_table WHERE id = :id")
    suspend fun remoteKeyStoryId(id: String): EntityStoryRemoteKey?

    @Query("DELETE FROM story_remote_key_table")
    suspend fun clearRemoteKeys()

}