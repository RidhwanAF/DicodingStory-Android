package com.raf.storyappsubmission.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.raf.storyappsubmission.api.ListStoryItemResponse
import com.raf.storyappsubmission.data.StoryRepository
import com.raf.storyappsubmission.model.UserModel
import com.raf.storyappsubmission.model.UserPreference
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepository: StoryRepository, private val pref: UserPreference) : ViewModel() {

    fun storyList(token: String): LiveData<PagingData<ListStoryItemResponse>> =
        storyRepository.getAllStories(token).cachedIn(viewModelScope)

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}