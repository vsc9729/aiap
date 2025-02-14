package com.synchronoss.aiap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CacheManagerTest {
    private lateinit var cacheManager: CacheManager
    private lateinit var mockContext: Context
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockDataStore = mockk()
        mockConnectivityManager = mockk()
        mockNetworkCapabilities = mockk()

        every { 
            mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) 
        } returns mockConnectivityManager

        cacheManager = CacheManager(mockContext)
        
        // Use reflection to set the mockDataStore
        val field = CacheManager::class.java.getDeclaredField("dataStore")
        field.isAccessible = true
        field.set(cacheManager, mockDataStore)
    }

    @Test
    fun `getCachedDataWithTimestamp returns cached data when timestamps match`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val cachedData = "cached_data"
        val preferences = mockk<Preferences>()
        
        every { preferences[stringPreferencesKey(key)] } returns cachedData
        every { preferences[longPreferencesKey("${key}_timestamp")] } returns timestamp
        coEvery { mockDataStore.data } returns flowOf(preferences)

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
        val preferences = mockk<Preferences>()
        
        every { preferences[stringPreferencesKey(key)] } returns null
        every { preferences[longPreferencesKey("${key}_timestamp")] } returns 0L
        coEvery { mockDataStore.data } returns flowOf(preferences)
        coEvery { 
            mockDataStore.updateData(any<suspend (Preferences) -> Preferences>())
        } returns mockk()

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
        
        // Verify updateData was called
        coVerify { mockDataStore.updateData(any()) }
    }

    @Test
    fun `getCachedDataWithNetwork returns network data when connected`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"
        val preferences = mockk<Preferences>()
        
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        
        coEvery { 
            mockDataStore.updateData(any<suspend (Preferences) -> Preferences>())
        } returns preferences

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
        
        // Verify updateData was called
        coVerify { mockDataStore.updateData(any()) }
    }

    @Test
    fun `getCachedDataWithNetwork returns cached data when network fails`() = runTest {
        // Given
        val key = "test_key"
        val cachedData = "cached_data"
        val preferences = mockk<Preferences>()

        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { preferences[stringPreferencesKey(key)] } returns cachedData
        coEvery { mockDataStore.data } returns flowOf(preferences)

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
        val preferences = mockk<Preferences>()
        
        every { mockConnectivityManager.activeNetwork } returns null
        every { mockConnectivityManager.getNetworkCapabilities(null) } returns null
        every { preferences[stringPreferencesKey(key)] } returns null
        coEvery { mockDataStore.data } returns flowOf(preferences)
        
        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { Resource.Success("data") },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("No cached data available and no internet 5", (result as Resource.Error).message)
    }

    @Test
    fun `clearCache clears all data`() = runTest {
        // Given
        val preferences = mockk<Preferences>()
        coEvery { 
            mockDataStore.updateData(any<suspend (Preferences) -> Preferences>())
        } returns preferences

        // When
        cacheManager.clearCache()

        // Then
        coVerify { mockDataStore.updateData(any()) }
    }

    @Test
    fun `getCachedDataWithTimestamp handles deserialization error`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val cachedData = "invalid_data"
        val preferences = mockk<Preferences>()
        
        every { preferences[stringPreferencesKey(key)] } returns cachedData
        every { preferences[longPreferencesKey("${key}_timestamp")] } returns timestamp
        coEvery { mockDataStore.data } returns flowOf(preferences)

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
        assertTrue((result as Resource.Error).message?.contains("Cache deserialization failed") == true)
    }

    @Test
    fun `getCachedDataWithNetwork handles exception during network fetch`() = runTest {
        // Given
        val key = "test_key"
        
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        // When
        val result = cacheManager.getCachedDataWithNetwork(
            key = key,
            fetchFromNetwork = { throw Exception("Network error") },
            serialize = { it },
            deserialize = { it }
        )

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }
} 