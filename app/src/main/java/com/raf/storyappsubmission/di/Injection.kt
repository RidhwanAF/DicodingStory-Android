package com.raf.storyappsubmission.di

import android.content.Context
import com.raf.storyappsubmission.api.ApiConfig
import com.raf.storyappsubmission.data.StoryRepository
import com.raf.storyappsubmission.db.StoryDatabase

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}