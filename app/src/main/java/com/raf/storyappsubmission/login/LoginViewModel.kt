package com.raf.storyappsubmission.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.raf.storyappsubmission.model.UserModel
import com.raf.storyappsubmission.model.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun saveUser(loginUser: UserModel) {
        viewModelScope.launch {
            pref.saveUser(loginUser)
        }
    }

    fun login() {
        viewModelScope.launch {
            pref.login()
        }
    }

}