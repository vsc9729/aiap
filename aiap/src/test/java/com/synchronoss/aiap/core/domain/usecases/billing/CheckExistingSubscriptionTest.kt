package com.synchronoss.aiap.core.domain.usecases.billing

import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckExistingSubscriptionTest {

    private lateinit var checkExistingSubscription: CheckExistingSubscription
    private lateinit var mockBillingManager: BillingManager
    private lateinit var mockErrorCallback: (String) -> Unit
    private lateinit var mockProductDetails: ProductDetails
    
    @Before
    fun setup() {
        mockBillingManager = mockk()
        mockErrorCallback = mockk(relaxed = true)
        mockProductDetails = mockk()
        
        checkExistingSubscription = CheckExistingSubscription(billingManager = mockBillingManager)
    }
    
    @Test
    fun `test invoke delegates to billingManager checkExistingSubscriptions when subscription exists`() = runTest {
        // Given
        coEvery { 
            mockBillingManager.checkExistingSubscriptions(onError = any())
        } returns mockProductDetails
        
        // When
        val result = checkExistingSubscription(onError = mockErrorCallback)
        
        // Then
        assertEquals(mockProductDetails, result)
        coVerify { mockBillingManager.checkExistingSubscriptions(onError = mockErrorCallback) }
    }
    
    @Test
    fun `test invoke delegates to billingManager checkExistingSubscriptions when no subscription exists`() = runTest {
        // Given
        coEvery { 
            mockBillingManager.checkExistingSubscriptions(onError = any())
        } returns null
        
        // When
        val result = checkExistingSubscription(onError = mockErrorCallback)
        
        // Then
        assertNull(result)
        coVerify { mockBillingManager.checkExistingSubscriptions(onError = mockErrorCallback) }
    }
}