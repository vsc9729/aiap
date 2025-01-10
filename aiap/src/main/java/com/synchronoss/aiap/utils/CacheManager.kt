package com.synchronoss.aiap.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("cache_store") }
    )
    
    suspend fun <T> getCachedDataWithTimestamp(
        key: String,
        currentTimestamp: Long?,
        fetchFromNetwork: suspend () -> Resource<T>,
        serialize: (T) -> String,
        deserialize: (String) -> T
    ): Resource<T> {
        try {
            // Check cache
            val preferences = dataStore.data.first()
            val dataKey = stringPreferencesKey(key)
            val timestampKey = longPreferencesKey("${key}_timestamp")
            
            val cachedValue: String? = preferences[dataKey]
            val cachedTimestamp: Long? = preferences[timestampKey]

            // If we have cached data and the timestamps match, return cached data
            if (cachedValue != null && cachedTimestamp != null && 
                currentTimestamp != null && cachedTimestamp == currentTimestamp) {
                return try {
                    Resource.Success(deserialize(cachedValue))
                } catch (e: Exception) {
                    Resource.Error("Cache deserialization failed: ${e.message}")
                }
            }

            // Fetch fresh data if no cache or timestamps don't match
            val networkResult = fetchFromNetwork()
            
            if (networkResult is Resource.Success && networkResult.data != null) {
                    // Update cache with new data and timestamp
                    try {
                        dataStore.edit { mutablePreferences ->
                            mutablePreferences[dataKey] = serialize(networkResult.data)
                            mutablePreferences[timestampKey] = currentTimestamp ?: System.currentTimeMillis()
                        }
                    } catch (e: Exception) {
                        return Resource.Error("Failed to save to cache: ${e.message}")
                    }
            }
            
            return networkResult
        } catch (e: Exception) {
            return Resource.Error("Cache manager error: ${e.message}")
        }
    }

    suspend fun clearCache() {
        dataStore.edit { it.clear() }
    }
} 