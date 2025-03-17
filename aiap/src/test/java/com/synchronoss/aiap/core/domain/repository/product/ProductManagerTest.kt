package com.synchronoss.aiap.core.domain.repository.product

import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.core.data.repository.product.ProductManagerImpl
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductManagerTest {
    @MockK
    private lateinit var productApi: ProductApi

    @MockK
    private lateinit var billingManager: BillingManager

    @MockK
    private lateinit var cacheManager: CacheManager

    private lateinit var productManager: ProductManager
    private val testApiKey = "test-api-key"

    companion object {
        private const val PRODUCTS_CACHE_KEY = "products_cache"
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        productManager = ProductManagerImpl(productApi, cacheManager)
    }

    @Test
    fun `getProducts returns cached data when timestamp is valid`() = runTest {
        // Given
        val mockProducts = listOf(
            ProductInfo(
                id = "test_id",
                productId = "test_product",
                displayName = "Test Product",
                description = "Test Description",
                vendorName = "Test Vendor",
                appName = "Test App",
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

        coEvery {
            cacheManager.getCachedDataWithTimestamp<List<ProductInfo>>(
                key = PRODUCTS_CACHE_KEY,
                currentTimestamp = 123L,
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } returns Resource.Success(mockProducts)

        // When
        val result = productManager.getProducts(123L, testApiKey)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockProducts, (result as Resource.Success).data)

        // Verify cache was used instead of network
        coVerify(exactly = 0) { productApi.getProducts(apiKey = testApiKey) }
    }

    @Test
    fun `getProducts returns all products from network`() = runTest {
        // Given
        val mockProducts =  ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "",
            data = listOf(
                ProductDataDto(
                    id = "test_id",
                    productId = "test_product_ios",
                    displayName = "Test Product iOS",
                    description = "Test Description",
                    vendorName = "Test Vendor",
                    appName = "Test App",
                    price = 9.99,
                    displayPrice = "$9.99",
                    platform = "IOS",
                    serviceLevel = "TEST_SERVICE",
                    isActive = true,
                    recurringPeriodCode = "P1M",
                    productType = "SUBSCRIPTION",
                    entitlementId = null
                ),
                ProductDataDto(
                    id = "test_id2",
                    productId = "test_product_android",
                    displayName = "Test Product Android",
                    description = "Test Description",
                    vendorName = "Test Vendor",
                    appName = "Test App",
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
        )

        coEvery { productApi.getProducts(apiKey = testApiKey) } returns mockProducts

        coEvery {
            cacheManager.getCachedDataWithTimestamp<List<ProductInfo>>(
                key = PRODUCTS_CACHE_KEY,
                currentTimestamp = null,
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            val fetch = thirdArg<(suspend () -> Resource<List<ProductInfo>>)>()
            fetch()
        }

        // When
        val result = productManager.getProducts(null, testApiKey)

        // Then
        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success<List<ProductInfo>>
        assertEquals(2, successResult.data?.size)
        // Verify both iOS and Android products are included
        assertTrue(successResult.data?.any { it.platform == "IOS" } == true)
        assertTrue(successResult.data?.any { it.platform == "ANDROID" } == true)
    }

    @Test
    fun `getProducts returns error when network call fails`() = runTest {
        // Given
        coEvery { productApi.getProducts(apiKey = testApiKey) } throws Exception("Network error")

        coEvery {
            cacheManager.getCachedDataWithTimestamp<List<ProductInfo>>(
                key = PRODUCTS_CACHE_KEY,
                currentTimestamp = null,
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            val fetch = thirdArg<(suspend () -> Resource<List<ProductInfo>>)>()
            fetch()
        }

        // When
        val result = productManager.getProducts(null, testApiKey)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `getActiveSubscription returns success with valid response`() = runTest {
        // Given
        val mockSubscriptionResponse = ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "",
            data = ActiveSubscriptionResponse(
                subscriptionResponseDTO = SubscriptionResponseDTO(
                    product = ProductDataDto(
                        id = "test_id",
                        productId = "test_product",
                        displayName = "Test Product",
                        description = "Test Description",
                        vendorName = "Test Vendor",
                        appName = "Test App",
                        price = 9.99,
                        displayPrice = "$9.99",
                        platform = "ANDROID",
                        serviceLevel = "TEST_SERVICE",
                        isActive = true,
                        recurringPeriodCode = "P1M",
                        productType = "SUBSCRIPTION",
                        entitlementId = null
                    ),
                    vendorName = "Test Vendor",
                    appName = "Test App",
                    appPlatformID = "TestAppAndroid",
                    platform = "ANDROID",
                    partnerUserId = "test-user-id",
                    startDate = 1234567890L,
                    endDate = 1234567890L,
                    status = "Active",
                    type = "SUBSCRIPTION"
                ),
                productUpdateTimeStamp = 1234567890L,
                themConfigTimeStamp = 1234567890L,
                userUUID = "test-user-uuid",
                baseServiceLevel = "basic",
                pendingPurchase = false
            )
        )

        val mockResponse = Response.success(mockSubscriptionResponse)

        coEvery {
            productApi.getActiveSubscription(apiKey = testApiKey, userId = "test-user-id")
        } returns mockResponse

        coEvery {
            cacheManager.getCachedDataWithNetwork<ActiveSubscriptionResponse>(
                key = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            val fetch = secondArg<(suspend () -> Resource<ActiveSubscriptionResponse>)>()
            fetch()
        }

        // When
        val result = productManager.getActiveSubscription("test-user-id", testApiKey)

        // Then
        assertTrue(result is Resource.Success)
        assertNotNull((result as Resource.Success).data)
        assertEquals("test_product", result.data?.subscriptionResponseInfo?.product?.productId)
    }


    @Test
    fun `getActiveSubscription returns error when network fails`() = runTest {
        // Given
        coEvery {
            productApi.getActiveSubscription(apiKey = testApiKey, userId = "test-user")
        } throws Exception("Network error")

        coEvery {
            cacheManager.getCachedDataWithNetwork<ActiveSubscriptionResponse>(
                key = any(),
                fetchFromNetwork = any(),
                serialize = any(),
                deserialize = any()
            )
        } coAnswers {
            val fetch = secondArg<(suspend () -> Resource<ActiveSubscriptionResponse>)>()
            fetch()
        }

        // When
        val result = productManager.getActiveSubscription("test-user", testApiKey)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }
}