package com.fh.theposition.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fh.theposition.data.repo.MainPrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainPrefViewModel @Inject constructor(private val repository: MainPrefRepository) :
    ViewModel() {

    fun saveLoginInfo(key: String, value: Boolean) = viewModelScope.launch {
        repository.saveLoginInfo(key, value)
    }

    fun isUserLoggedIn(key: String) = repository.isUserLoggedIn(key)

    fun saveName(key: String, value: String) = viewModelScope.launch {
        repository.saveName(key, value)
    }

    fun readName(key: String) = repository.readName(key)

    fun setLatitude(key: String, value: Double) = viewModelScope.launch {
        repository.setLatitude(key, value)
    }

    fun getLatitude(key: String) = repository.getLatitude(key)


    fun setLongitude(key: String, value: Double) = viewModelScope.launch {
        repository.setLongitude(key, value)
    }

    fun getLongitude(key: String) = repository.getLongitude(key)


    fun setScreenOn(key: String, value: Boolean) = viewModelScope.launch {
        repository.setScreenOn(key, value)
    }

    fun isScreenOn(key: String) = repository.isScreenOn(key)


    fun setTheme(key: String, value: String) = viewModelScope.launch {
        repository.setTheme(key, value)
    }

    fun getTheme(key: String) = repository.getTheme(key)

    fun setMapType(key: String, value: String) = viewModelScope.launch {
        repository.setMapType(key, value)
    }

    fun getMapType(key: String) = repository.getMapType(key)

}