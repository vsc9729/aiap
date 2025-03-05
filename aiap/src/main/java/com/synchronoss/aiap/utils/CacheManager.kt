package com.synchronoss.aiap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.synchronoss.aiap.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class CacheManager @Inject constructor(
    private val context: Context
) {
    private val cacheDir: File = context.cacheDir
    
    suspend fun <T> getCachedDataWithTimestamp(
        key: String,
        currentTimestamp: Long?,
        fetchFromNetwork: suspend () -> Resource<T>,
        serialize: (T) -> String,
        deserialize: (String) -> T
    ): Resource<T> {
        try {
            // Check cache
            val cachedData = getCachedFileData(key)
            val cachedTimestamp = getCachedTimestamp(key)
            
            // If we have cached data and the timestamps match, return cached data
            if (cachedData != null && cachedTimestamp != null && 
                currentTimestamp != null && cachedTimestamp == currentTimestamp) {
                return try {
                    Resource.Success(deserialize(cachedData))
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
                    saveCacheData(key, serialize(networkResult.data))
                    saveTimestamp(key, currentTimestamp ?: System.currentTimeMillis())
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
                            saveCacheData(key, serialize(data))
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
            val cachedData = getCachedFileData(key)
            if (cachedData != null) {
                try {
                    Resource.Success(deserialize(cachedData))
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

    private suspend fun saveCacheData(key: String, value: String) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, getDataFileName(key))
        FileOutputStream(file).use { 
            it.write(value.toByteArray())
        }
    }
    
    private suspend fun saveTimestamp(key: String, timestamp: Long) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, getTimestampFileName(key))
        FileOutputStream(file).use { 
            it.write(timestamp.toString().toByteArray())
        }
    }
    
    private suspend fun getCachedFileData(key: String): String? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, getDataFileName(key))
        if (file.exists()) {
            try {
                FileInputStream(file).use { fis ->
                    val bytes = ByteArray(file.length().toInt())
                    fis.read(bytes)
                    String(bytes)
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private suspend fun getCachedTimestamp(key: String): Long? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, getTimestampFileName(key))
        if (file.exists()) {
            try {
                FileInputStream(file).use { fis ->
                    val bytes = ByteArray(file.length().toInt())
                    fis.read(bytes)
                    String(bytes).toLongOrNull()
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private fun getDataFileName(key: String): String = "${key}_data"
    
    private fun getTimestampFileName(key: String): String = "${key}_timestamp"

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }
}