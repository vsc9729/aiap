package com.synchronoss.aiap.core.data.repository.product

import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseResponse
import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProductManagerImplTest {
    private lateinit var productApi: ProductApi
    private lateinit var cacheManager: CacheManager
    private lateinit var productManager: ProductManagerImpl
    private val testApiKey = "test-api-key"
    private val testUserId = "test-user-id"

    @Before
    fun setUp() {
        productApi = mockk()
        cacheManager = mockk()
        productManager = ProductManagerImpl(productApi, cacheManager)
    }

    @Test
    fun `test getActiveSubscription success`() = runBlocking {
        val mockResponse = ActiveSubscriptionResponse(
            subscriptionResponseDTO = SubscriptionResponseDTO(
                product = ProductDataDto(
                    id = "test_id",
                    productId = "test_product",
                    displayName = "Test Product",
                    description = "Test Description",
                    vendorName = "TestVendor",
                    appName = "TestApp",
                    price = 9.99,
                    displayPrice = "$9.99",
                    platform = "ANDROID",
                    serviceLevel = "TEST_SERVICE",
                    isActive = true,
                    recurringPeriodCode = "P1M",
                    productType = "SUBSCRIPTION",
                    entitlementId = null
                ),
                vendorName = "TestVendor",
                appName = "TestApp",
                appPlatformID = "TestAppAndroid",
                platform = "ANDROID",
                partnerUserId = "test-user",
                startDate = 1234567890L,
                endDate = 1234567891L,
                status = "Active",
                type = "SUBSCRIPTION"
            ),
            productUpdateTimeStamp = 1234567890L,
            themConfigTimeStamp = 1234567891L,
            userUUID = "test-user-uuid",
            baseServiceLevel = "basic",
            pendingPurchase = false
        )

        val apiResponse = ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "Success",
            data = mockResponse
        )

        coEvery { 
            cacheManager.getCachedDataWithNetwork<ActiveSubscriptionInfo>(
                key = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers { 
            Resource.Success(mockk<ActiveSubscriptionInfo>(relaxed = true) {
                every { userUUID } returns "test-user-uuid"
            })
        }

        coEvery { 
            productApi.getActiveSubscription(testApiKey, testUserId) 
        } returns Response.success(apiResponse)

        val result = productManager.getActiveSubscription(testUserId, testApiKey)
        assertNotNull(result)
        val data = (result as Resource.Success).data
        assertNotNull(data)
        assertEquals("test-user-uuid", data!!.userUUID)
    }

    @Test
    fun `test getProducts success`() = runBlocking {
        val mockProducts = listOf(
            ProductDataDto(
                id = "test_id",
                productId = "test_product",
                displayName = "Test Product",
                description = "Test Description",
                vendorName = "TestVendor",
                appName = "TestApp",
                price = 9.99,
                displayPrice = "$9.99",
                platform = "ANDROID",
                serviceLevel = "TEST_SERVICE",
                isActive = true,
                recurringPeriodCode = "P1M",
                productType = "SUBSCRIPTION",
                entitlementId = null
            )
        )

        val apiResponse = ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "Success",
            data = mockProducts
        )

        coEvery {
            cacheManager.getCachedDataWithTimestamp<List<ProductInfo>>(
                key = any(),
                currentTimestamp = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            Resource.Success(listOf(mockk<ProductInfo>(relaxed = true)))
        }

        coEvery { productApi.getProducts(testApiKey) } returns apiResponse

        val result = productManager.getProducts(null, testApiKey)
        assertNotNull(result)
        val data = (result as Resource.Success).data
        assertNotNull(data)
        assertEquals(1, data!!.size)
    }

    @Test
    fun `test handlePurchase success`() = runBlocking {
        val request = HandlePurchaseRequest(
            productId = "test_product",
            purchaseToken = "test_token",
            purchaseTime = 1234567890L,
            partnerUserId = "test_user"
        )

        val mockPurchaseResponse = HandlePurchaseResponse(
            product = ProductDataDto(
                id = "test_id",
                productId = "test_product",
                displayName = "Test Product",
                description = "Test Description",
                vendorName = "TestVendor",
                appName = "TestApp",
                price = 9.99,
                displayPrice = "$9.99",
                platform = "ANDROID",
                serviceLevel = "TEST_SERVICE",
                isActive = true,
                recurringPeriodCode = "P1M",
                productType = "SUBSCRIPTION",
                entitlementId = null
            ),
            vendorName = "TestVendor",
            appName = "TestApp",
            appPlatformID = "TestAppAndroid",
            platform = "ANDROID",
            partnerUserId = "test-user",
            startDate = 1234567890L,
            endDate = 1234567891L,
            status = "Active",
            type = "SUBSCRIPTION"
        )

        val apiResponse = ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "Success",
            data = mockPurchaseResponse
        )

        coEvery { 
            productApi.handlePurchase(request, testApiKey) 
        } returns Response.success(apiResponse)

        val result = productManager.handlePurchase(request, testApiKey)
        assertTrue(result)
    }
}