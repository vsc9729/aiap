package com.synchronoss.aiap.core.domain.usecases.billing

import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandleUnacknowledgedPurchasesTest {

    private lateinit var handleUnacknowledgedPurchases: HandleUnacknowledgedPurchases
    private lateinit var mockBillingManager: BillingManager
    private lateinit var mockErrorCallback: (String) -> Unit
    
    @Before
    fun setup() {
        mockBillingManager = mockk()
        mockErrorCallback = mockk(relaxed = true)
        
        handleUnacknowledgedPurchases = HandleUnacknowledgedPurchases(billingManager = mockBillingManager)
    }
    
    @Test
    fun `test invoke delegates to billingManager handleUnacknowledgedPurchases when purchases exist`() = runTest {
        // Given
        coEvery { 
            mockBillingManager.handleUnacknowledgedPurchases(onError = any())
        } returns true
        
        // When
        val result = handleUnacknowledgedPurchases(onError = mockErrorCallback)
        
        // Then
        assertTrue(result)
        coVerify { mockBillingManager.handleUnacknowledgedPurchases(onError = mockErrorCallback) }
    }
    
    @Test
    fun `test invoke delegates to billingManager handleUnacknowledgedPurchases when no purchases exist`() = runTest {
        // Given
        coEvery { 
            mockBillingManager.handleUnacknowledgedPurchases(onError = any())
        } returns false
        
        // When
        val result = handleUnacknowledgedPurchases(onError = mockErrorCallback)
        
        // Then
        assertEquals(false, result)
        coVerify { mockBillingManager.handleUnacknowledgedPurchases(onError = mockErrorCallback) }
    }
}