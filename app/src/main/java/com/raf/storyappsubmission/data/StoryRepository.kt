package com.raf.storyappsubmission.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.raf.storyappsubmission.api.ApiService
import com.raf.storyappsubmission.api.ListStoryItemResponse
import com.raf.storyappsubmission.db.StoryDatabase


class StoryRepository(private val storyDatabase: StoryDatabase, private val apiService: ApiService
) {

    fun getAllStories(token: String): LiveData<PagingData<ListStoryItemResponse>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }
}