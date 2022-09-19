package com.example.intermediatesubmission.common.di.app

import android.content.Context
import androidx.room.Room
import com.example.intermediatesubmission.data.local.AppDatabase
import com.example.intermediatesubmission.data.local.dao.StoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "story_database").build()

    @Singleton
    @Provides
    fun provideStoryDao(appDatabase: AppDatabase): StoryDao =
        appDatabase.storyDao()

    @Singleton
    @Provides
    fun provideStoryRemoteKeyDao(appDatabase: AppDatabase) =
        appDatabase.storyRemoteKeyDao()

}