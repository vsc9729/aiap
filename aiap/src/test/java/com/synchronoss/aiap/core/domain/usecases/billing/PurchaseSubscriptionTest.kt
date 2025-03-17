package com.synchronoss.aiap.core.domain.usecases.billing

import androidx.activity.ComponentActivity
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PurchaseSubscriptionTest {

    private lateinit var purchaseSubscription: PurchaseSubscription
    private lateinit var mockBillingManager: BillingManager
    private lateinit var mockActivity: ComponentActivity
    private lateinit var mockProductDetails: ProductDetails
    private lateinit var mockErrorCallback: (String) -> Unit
    
    private val testUserId = "test_user_id"
    private val testApiKey = "test_api_key"
    
    @Before
    fun setup() {
        mockBillingManager = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockProductDetails = mockk(relaxed = true)
        mockErrorCallback = mockk(relaxed = true)
        
        purchaseSubscription = PurchaseSubscription(billingManager = mockBillingManager)
    }
    
    @Test
    fun `test invoke delegates to billingManager purchaseSubscription`() = runTest {
        // Given
        coEvery { 
            mockBillingManager.purchaseSubscription(
                activity = any(),
                productDetails = any(),
                onError = any(),
                userId = any(),
                apiKey = any()
            )
        } returns Unit
        
        // When
        purchaseSubscription(
            activity = mockActivity,
            product = mockProductDetails,
            onError = mockErrorCallback,
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Then
        coVerify { 
            mockBillingManager.purchaseSubscription(
                activity = mockActivity,
                productDetails = mockProductDetails,
                onError = mockErrorCallback,
                userId = testUserId,
                apiKey = testApiKey
            )
        }
    }
}