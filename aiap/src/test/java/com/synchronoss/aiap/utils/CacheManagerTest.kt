package com.synchronoss.aiap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CacheManagerTest {
    private lateinit var cacheManager: CacheManager
    private lateinit var mockContext: Context
    private lateinit var tempCacheDir: File
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        // Create a temporary directory for testing
        tempCacheDir = createTempDir(prefix = "test_cache")
        mockConnectivityManager = mockk(relaxed = true)
        mockNetworkCapabilities = mockk(relaxed = true)

        every { mockContext.getString(any()) } returns "Test error message"
        every { mockContext.getString(any(), any()) } returns "Test error message with param"
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        every { mockContext.cacheDir } returns tempCacheDir
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(any()) } returns true
        
        cacheManager = CacheManager(mockContext)
    }

    @After
    fun tearDown() {
        // Clean up the temporary directory after each test
        tempCacheDir.deleteRecursively()
    }

    @Test
    fun `getCachedDataWithTimestamp returns cached data when timestamps match`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val cachedData = "cached_data"

        // Create and populate cache files
        File(tempCacheDir, "${key}_data").writeText(cachedData)
        File(tempCacheDir, "${key}_timestamp").writeText(timestamp.toString())

        // When
        val result = cacheManager.getCachedDataWithTimestamp(
            key = key,
            currentTimestamp = timestamp,
            fetchFromNetwork = { Resource.Success("network_data") },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(cachedData, (result as Resource.Success).data)
    }

    @Test
    fun `getCachedDataWithTimestamp fetches from network when timestamps don't match`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"
        val currentTimestamp = 123456789L
        val oldTimestamp = 123L

        // Create cache files with old timestamp
        File(tempCacheDir, "${key}_data").writeText("old_data")
        File(tempCacheDir, "${key}_timestamp").writeText(oldTimestamp.toString())

        // When
        val result = cacheManager.getCachedDataWithTimestamp(
            key = key,
            currentTimestamp = currentTimestamp,
            fetchFromNetwork = { Resource.Success(networkData) },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(networkData, (result as Resource.Success).data)
        
        // Verify new data was cached
        assertEquals(networkData, File(tempCacheDir, "${key}_data").readText())
        assertEquals(currentTimestamp.toString(), File(tempCacheDir, "${key}_timestamp").readText())
    }

    @Test
    fun `getCachedDataWithNetwork returns network data when connected`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"

        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { Resource.Success(networkData) },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(networkData, (result as Resource.Success).data)
        
        // Verify data was cached
        assertEquals(networkData, File(tempCacheDir, "${key}_data").readText())
    }

    @Test
    fun `getCachedDataWithNetwork returns cached data when network fails`() = runTest {
        // Given
        val key = "test_key"
        val cachedData = "cached_data"

        // Create cache file with data
        File(tempCacheDir, "${key}_data").writeText(cachedData)

        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { Resource.Error("Network failed") },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(cachedData, (result as Resource.Success).data)
    }

    @Test
    fun `getCachedDataWithNetwork returns error when no network and no cache`() = runTest {
        // Given
        val key = "test_key"
        
        every { mockConnectivityManager.activeNetwork } returns null
        every { mockConnectivityManager.getNetworkCapabilities(null) } returns null

        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { Resource.Success("data") },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `getCachedDataWithTimestamp handles deserialization error`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val invalidData = "invalid_data"

        // Create cache files with invalid data
        File(tempCacheDir, "${key}_data").writeText(invalidData)
        File(tempCacheDir, "${key}_timestamp").writeText(timestamp.toString())

        // When
        val result = cacheManager.getCachedDataWithTimestamp(
            key = key,
            currentTimestamp = timestamp,
            fetchFromNetwork = { Resource.Success("network_data") },
            serialize = { it },
            deserialize = { throw Exception("Deserialization failed") }
        )

        // Then
        assertTrue(result is Resource.Error)
    }

    @Test
    fun `getCachedDataWithNetwork handles cellular network`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"
        
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true

        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { Resource.Success(networkData) },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(networkData, (result as Resource.Success).data)
        
        // Verify data was cached
        assertEquals(networkData, File(tempCacheDir, "${key}_data").readText())
    }

    @Test
    fun `getCachedDataWithTimestamp handles network error and cache save error`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"

        // Make the cache directory read-only to simulate write error
        tempCacheDir.setReadOnly()

        // When
        val result = cacheManager.getCachedDataWithTimestamp(
            key = key,
            currentTimestamp = 123L,
            fetchFromNetwork = { Resource.Success(networkData) },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Error)
        
        // Reset directory permissions for cleanup
        tempCacheDir.setWritable(true)
    }

    @Test
    fun `clearCache clears all files`() = runTest {
        // Given
        val files = listOf("file1", "file2", "file3")
        files.forEach { 
            File(tempCacheDir, it).writeText("test data")
        }

        // When
        cacheManager.clearCache()

        // Then
        assertEquals(0, tempCacheDir.listFiles()?.size ?: -1)
    }
}
