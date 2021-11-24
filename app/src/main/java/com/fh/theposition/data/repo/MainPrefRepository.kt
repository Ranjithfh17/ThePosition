package com.fh.theposition.data.repo

import com.fh.theposition.util.MainPreference
import javax.inject.Inject

class MainPrefRepository @Inject constructor(private val mainPreference: MainPreference) {

    suspend fun saveLoginInfo(key: String, value: Boolean) = mainPreference.saveLoginInfo(key, value)
    fun isUserLoggedIn(key: String) = mainPreference.isUserLoggedIn(key)


    suspend fun saveName(key: String, value: String) = mainPreference.saveName(key, value)
    fun readName(key: String) = mainPreference.readName(key)

    suspend fun setLatitude(key: String,value: Double) = mainPreference.setLatitude(key, value)
    fun getLatitude(key: String) = mainPreference.getLatitude(key)

    suspend fun setLongitude(key: String,value: Double) = mainPreference.setLongitude(key, value)
    fun getLongitude(key: String) = mainPreference.getLongitude(key)

    suspend fun setScreenOn(key: String,value: Boolean)=mainPreference.setScreenOn(key, value)
    fun isScreenOn(key:String)=mainPreference.isScreenOn(key)

    suspend fun setTheme(key: String,value: String)=mainPreference.setTheme(key, value)
    fun getTheme(key:String)=mainPreference.getTheme(key)


    suspend fun setMapType(key: String,value: String)=mainPreference.setMapType(key, value)
    fun getMapType(key:String)=mainPreference.getMapType(key)


}