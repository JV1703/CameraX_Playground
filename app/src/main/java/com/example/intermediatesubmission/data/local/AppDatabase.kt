package com.example.intermediatesubmission.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.intermediatesubmission.data.local.dao.StoryDao
import com.example.intermediatesubmission.data.local.dao.StoryRemoteKeyDao
import com.example.intermediatesubmission.data.local.entity.EntityStory
import com.example.intermediatesubmission.data.local.entity.EntityStoryRemoteKey

@Database(entities = [EntityStory::class, EntityStoryRemoteKey::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun storyRemoteKeyDao(): StoryRemoteKeyDao

}