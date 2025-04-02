package com.synchronoss.aiap.utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse

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
    
    @Test
    fun `test Success with non-null data should return data hashCode`() {
        val data = "test_data"
        val success = Resource.Success(data)
        
        assertEquals(data.hashCode(), success.hashCode())
    }
    
    @Test
    fun `test Success equals with same reference`() {
        val success = Resource.Success("data")
        
        // Test "this === other" condition (same reference)
        assertEquals(success, success)
    }
    
    @Test
    fun `test Success equals with different types`() {
        val success = Resource.Success<String>("data")
        val error = Resource.Error<String>("message")
        
        // Test "other !is Success<*>" condition
        assertFalse(success.equals(error))
        assertFalse(success.equals("not a Resource"))
        assertFalse(success.equals(null))
    }
    
    @Test
    fun `test Success equals with different data`() {
        val success1 = Resource.Success<String>("data1")
        val success2 = Resource.Success<String>("data2")
        val success3 = Resource.Success<String>("data1")
        val successNull1: Resource.Success<String?> = Resource.Success(null) 
        val successNull2: Resource.Success<String?> = Resource.Success(null)
        
        // Test "data == other.data" condition
        assertNotEquals(success1, success2)
        assertEquals(success1, success3)
        
        // Test with null data
        assertEquals(successNull1, successNull2)
        assertNotEquals(successNull1, success1 as Resource<String?>)
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
        
        // Test with null message (indirect since constructor doesn't allow null, but hashCode implementation handles it)
        val error3: Resource.Error<String?> = Resource.Error("", null)
        val error4: Resource.Error<String?> = Resource.Error("", "data")
        assertNotEquals(error3.hashCode(), error4.hashCode())
    }
    
    @Test
    fun `test Error hashCode with non-null message and data`() {
        val message = "error_message"
        val data = "error_data"
        
        val error1 = Resource.Error(message, data)
        val error2 = Resource.Error(message, data)
        
        // Same message and data should result in same hashCode
        assertEquals(error1.hashCode(), error2.hashCode())
        
        // Different message or data should result in different hashCode
        val error3 = Resource.Error("different", data)
        val error4 = Resource.Error(message, "different")
        
        assertNotEquals(error1.hashCode(), error3.hashCode())
        assertNotEquals(error1.hashCode(), error4.hashCode())
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
    
    @Test
    fun `test Error equals with same reference`() {
        val error = Resource.Error<String>("message", "data")
        
        // Test "this === other" condition (same reference)
        assertEquals(error, error)
    }
    
    @Test
    fun `test Error equals with different types`() {
        val error = Resource.Error<String>("message", "data")
        val success = Resource.Success<String>("data")
        
        // Test "other !is Error<*>" condition
        assertFalse(error.equals(success))
        assertFalse(error.equals("not a Resource"))
        assertFalse(error.equals(null))
    }
    
    @Test
    fun `test Error equals with different message and data combinations`() {
        val message = "error_message"
        val data = "error_data"
        
        val error1 = Resource.Error<String>(message, data)
        val error2 = Resource.Error<String>(message, data)
        val error3 = Resource.Error<String>("different", data)
        val error4 = Resource.Error<String>(message, "different")
        val error5 = Resource.Error<String>("different", "different")
        
        // Different combinations for error message/data equality
        assertEquals(error1, error2)  // Same message, same data
        assertNotEquals(error1, error3)  // Different message, same data
        assertNotEquals(error1, error4)  // Same message, different data
        assertNotEquals(error1, error5)  // Different message, different data
        
        // Additional tests with nulls - since message cannot be null due to constructor,
        // we use empty string as a proxy
        val errorEmptyMsg1 = Resource.Error<String>("", data)
        val errorEmptyMsg2 = Resource.Error<String>("", data)
        assertEquals(errorEmptyMsg1, errorEmptyMsg2)
        assertNotEquals(errorEmptyMsg1, error1)
    }
    
    @Test
    fun `test Error equals with all nulls and empty values`() {
        // Test with empty strings and nulls to increase branch coverage
        // Note: message can't be null due to constructor, so we use empty string
        val error1 = Resource.Error<String?>("", null)
        val error2 = Resource.Error<String?>("", null)
        val error3 = Resource.Error<String>("", "")
        
        assertEquals(error1, error2)  // Same empty message, same null data
        assertNotEquals(error1, error3 as Resource<String?>)  // Same empty message, different data
    }
    
    @Test
    fun `test Resource parent class constructor`() {
        // Testing the base Resource class constructor through subclasses
        val success = Resource.Success<String>("test")
        assertEquals("test", success.data)
        assertNull(success.message)
        
        val error = Resource.Error<String>("error_message", "test_data")
        assertEquals("test_data", error.data)
        assertEquals("error_message", error.message)
    }
} 