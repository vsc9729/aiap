package com.synchronoss.aiap.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ResourceTest {
    
    // Test Success class
    
    @Test
    fun `test Success with null data should return 0 for hashCode`() {
        // We can only test this by checking the public contract
        // Two Success objects with null data should have the same hashCode
        val success1: Resource.Success<String?> = Resource.Success(null)
        val success2: Resource.Success<String?> = Resource.Success(null)
        
        assertEquals(0, success1.hashCode())
        assertEquals(success1.hashCode(), success2.hashCode())
    }
    
    // Test Error class
    
    @Test
    fun `test Error constructor with only message parameter`() {
        val message = "error_message"
        val error: Resource.Error<String?> = Resource.Error(message)
        
        assertEquals(message, error.message)
        assertNull(error.data)
    }
    
    @Test
    fun `test Error constructor with message and data`() {
        val message = "error_message"
        val data = "error_data"
        
        // This directly calls the constructor with both parameters
        val error = Resource.Error(message, data)
        
        assertEquals(message, error.message)
        assertEquals(data, error.data)
    }
    
    @Test
    fun `test Error hashCode with null values`() {
        // Test with null data
        val errorWithNullData1: Resource.Error<String?> = Resource.Error("error", null)
        val errorWithNullData2: Resource.Error<String?> = Resource.Error("error", null)
        assertEquals(errorWithNullData1.hashCode(), errorWithNullData2.hashCode())
        
        // Different errors with different messages should have different hashCodes
        val error1: Resource.Error<String?> = Resource.Error("error1", null)
        val error2: Resource.Error<String?> = Resource.Error("error2", null)
        assertNotEquals(error1.hashCode(), error2.hashCode())
    }
    
    @Test
    fun `test Error equals with null data`() {
        // Test equals with null data
        val error1: Resource.Error<String?> = Resource.Error("error", null)
        val error2: Resource.Error<String?> = Resource.Error("error", null)
        val error3: Resource.Error<String?> = Resource.Error("different", null)
        
        assertEquals(error1, error2)
        assertNotEquals(error1, error3)
    }
} 