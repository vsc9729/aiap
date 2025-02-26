package com.synchronoss.aiap.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.synchronoss.aiap.R

@Singleton
class CacheManager @Inject constructor(
    private val context: Context
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
                    Resource.Error(
                        context.getString(
                            R.string.cache_deserialization_failed,
                            e.message
                        )
                    )
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
                        return Resource.Error(
                            context.getString(
                                R.string.cache_save_failed,
                                e.message
                            )
                        )
                    }
            }
            
            return networkResult
        } catch (e: Exception) {
            return Resource.Error(context.getString(R.string.cache_manager_error, e.message))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    suspend fun <T> getCachedDataWithNetwork(
        key: String,
        fetchFromNetwork: suspend () -> Resource<T>,
        serialize: (T) -> String,
        deserialize: (String) -> T
    ): Resource<T> {
        return try {
            if (isNetworkAvailable()) {
                // If network is available, fetch fresh data and cache it
                when (val result = fetchFromNetwork()) {
                    is Resource.Success -> {
                        result.data?.let { data ->
                            saveData(key, serialize(data))
                        }
                        result
                    }
                    is Resource.Error -> {
                        // If network call fails, try to get cached data
                        getCachedData(key, deserialize)?.takeIf { it is Resource.Success }
                            ?: Resource.Error(context.getString(R.string.no_cached_data_request_failed))
                    }
                }
            } else {
                // If no network, return cached data
                getCachedData(key, deserialize)?.takeIf { it is Resource.Success }
                    ?: Resource.Error(context.getString(R.string.no_cached_data_no_internet))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.unknown_error))
        }
    }

    private suspend fun <T> getCachedData(
        key: String,
        deserialize: (String) -> T
    ): Resource<T>? {
        return try {
            val preferences = dataStore.data.first()
            val cachedValue = preferences[stringPreferencesKey(key)]
            if (cachedValue != null) {
                try {
                    Resource.Success(deserialize(cachedValue))
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveData(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    suspend fun clearCache() {
        dataStore.edit { it.clear() }
    }
}