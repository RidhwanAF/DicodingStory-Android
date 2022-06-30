package com.raf.storyappsubmission.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raf.storyappsubmission.api.ListStoryItemResponse

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItemResponse>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItemResponse>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}