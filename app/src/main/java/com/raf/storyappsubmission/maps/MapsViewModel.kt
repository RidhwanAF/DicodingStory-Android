package com.raf.storyappsubmission.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.raf.storyappsubmission.api.ApiConfig.Companion.getApiService
import com.raf.storyappsubmission.api.ListStoryItemResponse
import com.raf.storyappsubmission.api.StoriesResponse
import com.raf.storyappsubmission.model.UserModel
import com.raf.storyappsubmission.model.UserPreference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val pref: UserPreference, private val location: Int) : ViewModel() {

    private val _ListStories = MutableLiveData<List<ListStoryItemResponse>>()
    val listStories: LiveData<List<ListStoryItemResponse>> = _ListStories

    fun getStories(token: String) {
        val service = getApiService().getAllMapStories(location, "bearer $token")
        service.enqueue(object: Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    _ListStories.value = responseBody?.listStory
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}