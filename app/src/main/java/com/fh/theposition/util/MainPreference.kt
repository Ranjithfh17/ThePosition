package com.fh.theposition.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class MainPreference(private val context: Context) {


    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREF_NAME)

    suspend fun saveLoginInfo(key: String, value: Boolean) {
        val dataStoreKey = booleanPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] = value
        }
    }

     fun isUserLoggedIn(key:String):Flow<Boolean> = context.dataStore.data.map {
        val dataStoreKey = booleanPreferencesKey(key)
        val hi=it[dataStoreKey] ?: false
        hi
    }

    suspend fun saveName(key:String,value:String){
        val dataStoreKey= stringPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] =value
        }
    }

    fun readName(key:String):Flow<String> = context.dataStore.data.map {
        val dateStoreKey= stringPreferencesKey(key)
        val name=it[dateStoreKey] ?: "No name"
        name
    }


    suspend fun setLatitude(key: String,value: Double){
        val dataStoreKey= doublePreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] =value
        }
    }

    fun getLatitude(key: String):Flow<Double> = context.dataStore.data.map {
        val dataStoreKey= doublePreferencesKey(key)
        val latitude = it[dataStoreKey] ?: 0.0
        latitude
    }

    suspend fun setLongitude(key: String,value:Double){
        val dataStoreKey= doublePreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] =value
        }
    }

    fun getLongitude(key: String):Flow<Double> = context.dataStore.data.map {
        val dateStoreKey = doublePreferencesKey(key)
        val longitude =it[dateStoreKey] ?: 0.0
        longitude
    }

    suspend fun setScreenOn(key:String,value:Boolean){
        val dataStoreKey= booleanPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey]= value
        }
    }

    fun isScreenOn(key:String):Flow<Boolean> = context.dataStore.data.map {
        val dataStoreKey= booleanPreferencesKey(key)
        val isScreenOnEnabled=it[dataStoreKey] ?: false
        isScreenOnEnabled
    }

    suspend fun setTheme(key: String,value:String){
        val dataStoreKey= stringPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey]=value
        }
    }

    fun getTheme(key: String):Flow<String> = context.dataStore.data.map {
        val dataStoreKey= stringPreferencesKey(key)
        val theme=it[dataStoreKey] ?: "followSystem"
        theme
    }

    suspend fun setMapType(key: String,value: String){
        val dataStoreKey= stringPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] =value
        }
    }

    fun getMapType(key: String):Flow<String> = context.dataStore.data.map {
        val dataStoreKey= stringPreferencesKey(key)
        val type=it[dataStoreKey] ?: "Normal"
        type
    }



    companion object {
        const val PREF_NAME = "main_pref"
    }


}