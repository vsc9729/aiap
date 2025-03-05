package com.synchronoss.aiap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CacheManagerTest {
    private lateinit var cacheManager: CacheManager
    private lateinit var mockContext: Context
    private lateinit var mockCacheDir: File
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockCacheDir = mockk(relaxed = true)
        mockConnectivityManager = mockk()
        mockNetworkCapabilities = mockk()

        every { 
            mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) 
        } returns mockConnectivityManager
        
        every { mockContext.cacheDir } returns mockCacheDir
        
        cacheManager = CacheManager(mockContext)
    }

    @Test
    fun `getCachedDataWithTimestamp returns cached data when timestamps match`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val cachedData = "cached_data"
        val dataFile = mockk<File>(relaxed = true)
        val timestampFile = mockk<File>(relaxed = true)
        val dataFileInputStream = mockk<FileInputStream>()
        val timestampFileInputStream = mockk<FileInputStream>()
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { File(mockCacheDir, "${key}_timestamp") } returns timestampFile
        every { dataFile.exists() } returns true
        every { timestampFile.exists() } returns true
        every { dataFile.length() } returns cachedData.length.toLong()
        every { timestampFile.length() } returns timestamp.toString().length.toLong()
        
        every { FileInputStream(dataFile) } returns dataFileInputStream
        every { FileInputStream(timestampFile) } returns timestampFileInputStream
        
        every { dataFileInputStream.read(any()) } answers {
            val byteArray = firstArg<ByteArray>()
            cachedData.toByteArray().copyInto(byteArray)
            cachedData.length
        }
        
        every { timestampFileInputStream.read(any()) } answers {
            val byteArray = firstArg<ByteArray>()
            timestamp.toString().toByteArray().copyInto(byteArray)
            timestamp.toString().length
        }
        
        every { dataFileInputStream.close() } just Runs
        every { timestampFileInputStream.close() } just Runs

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
        val dataFile = mockk<File>(relaxed = true)
        val timestampFile = mockk<File>(relaxed = true)
        val mockFileOutputStream = mockk<FileOutputStream>(relaxed = true)
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { File(mockCacheDir, "${key}_timestamp") } returns timestampFile
        every { dataFile.exists() } returns false
        every { timestampFile.exists() } returns false
        
        every { FileOutputStream(dataFile) } returns mockFileOutputStream
        every { FileOutputStream(timestampFile) } returns mockFileOutputStream

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
        
        // Verify files were written
        verify { FileOutputStream(dataFile) }
        verify { FileOutputStream(timestampFile) }
        verify { mockFileOutputStream.write(any<ByteArray>()) }
    }

    @Test
    fun `getCachedDataWithNetwork returns network data when connected`() = runTest {
        // Given
        val key = "test_key"
        val networkData = "network_data"
        val dataFile = mockk<File>(relaxed = true)
        val mockFileOutputStream = mockk<FileOutputStream>(relaxed = true)
        
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { FileOutputStream(dataFile) } returns mockFileOutputStream

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
        
        // Verify file was written
        verify { FileOutputStream(dataFile) }
        verify { mockFileOutputStream.write(any<ByteArray>()) }
    }

    @Test
    fun `getCachedDataWithNetwork returns cached data when network fails`() = runTest {
        // Given
        val key = "test_key"
        val cachedData = "cached_data"
        val dataFile = mockk<File>(relaxed = true)
        val dataFileInputStream = mockk<FileInputStream>()
        
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { dataFile.exists() } returns true
        every { dataFile.length() } returns cachedData.length.toLong()
        
        every { FileInputStream(dataFile) } returns dataFileInputStream
        
        every { dataFileInputStream.read(any()) } answers {
            val byteArray = firstArg<ByteArray>()
            cachedData.toByteArray().copyInto(byteArray)
            cachedData.length
        }
        
        every { dataFileInputStream.close() } just Runs

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
        val dataFile = mockk<File>(relaxed = true)
        
        every { mockConnectivityManager.activeNetwork } returns null
        every { mockConnectivityManager.getNetworkCapabilities(null) } returns null
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { dataFile.exists() } returns false
        
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
    fun `clearCache clears all files`() = runTest {
        // Given
        val file1 = mockk<File>(relaxed = true)
        val file2 = mockk<File>(relaxed = true)
        
        every { mockCacheDir.listFiles() } returns arrayOf(file1, file2)

        // When
        cacheManager.clearCache()

        // Then
        verify { file1.delete() }
        verify { file2.delete() }
    }

    @Test
    fun `getCachedDataWithTimestamp handles deserialization error`() = runTest {
        // Given
        val key = "test_key"
        val timestamp = 123L
        val cachedData = "invalid_data"
        val dataFile = mockk<File>(relaxed = true)
        val timestampFile = mockk<File>(relaxed = true)
        val dataFileInputStream = mockk<FileInputStream>()
        val timestampFileInputStream = mockk<FileInputStream>()
        
        every { File(mockCacheDir, "${key}_data") } returns dataFile
        every { File(mockCacheDir, "${key}_timestamp") } returns timestampFile
        every { dataFile.exists() } returns true
        every { timestampFile.exists() } returns true
        every { dataFile.length() } returns cachedData.length.toLong()
        every { timestampFile.length() } returns timestamp.toString().length.toLong()
        
        every { FileInputStream(dataFile) } returns dataFileInputStream
        every { FileInputStream(timestampFile) } returns timestampFileInputStream
        
        every { dataFileInputStream.read(any()) } answers {
            val byteArray = firstArg<ByteArray>()
            cachedData.toByteArray().copyInto(byteArray)
            cachedData.length
        }
        
        every { timestampFileInputStream.read(any()) } answers {
            val byteArray = firstArg<ByteArray>()
            timestamp.toString().toByteArray().copyInto(byteArray)
            timestamp.toString().length
        }
        
        every { dataFileInputStream.close() } just Runs
        every { timestampFileInputStream.close() } just Runs

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