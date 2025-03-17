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

class GetProductDetailsTest {

    private lateinit var getProductDetails: GetProductDetails
    private lateinit var mockBillingManager: BillingManager
    private lateinit var mockErrorCallback: (String) -> Unit
    private lateinit var mockProductDetailsList: List<ProductDetails>
    
    @Before
    fun setup() {
        mockBillingManager = mockk()
        mockErrorCallback = mockk(relaxed = true)
        mockProductDetailsList = listOf(mockk(), mockk())
        
        getProductDetails = GetProductDetails(billingManager = mockBillingManager)
    }
    
    @Test
    fun `test invoke delegates to billingManager getProductDetails`() = runTest {
        // Given
        val productIds = listOf("product_1", "product_2")
        coEvery { 
            mockBillingManager.getProductDetails(
                productIds = any(),
                onError = any()
            )
        } returns mockProductDetailsList
        
        // When
        val result = getProductDetails(
            productIds = productIds,
            onError = mockErrorCallback
        )
        
        // Then
        assertEquals(mockProductDetailsList, result)
        coVerify { 
            mockBillingManager.getProductDetails(
                productIds = productIds,
                onError = mockErrorCallback
            )
        }
    }
    
    @Test
    fun `test invoke handles null result from billingManager`() = runTest {
        // Given
        val productIds = listOf("product_1", "product_2")
        coEvery { 
            mockBillingManager.getProductDetails(
                productIds = any(),
                onError = any()
            )
        } returns null
        
        // When
        val result = getProductDetails(
            productIds = productIds,
            onError = mockErrorCallback
        )
        
        // Then
        assertEquals(null, result)
    }
}