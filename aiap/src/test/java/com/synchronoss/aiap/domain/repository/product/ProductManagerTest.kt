import com.synchronoss.aiap.data.remote.common.ApiResponse
import com.synchronoss.aiap.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.data.remote.product.ProductDataDto
import com.synchronoss.aiap.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.data.repository.product.ProductMangerImpl
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import kotlinx.coroutines.runBlocking


import io.mockk.MockKAnnotations.init
import io.mockk.coVerify

import io.mockk.impl.annotations.MockK

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull


import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.mockk.coEvery

class ProductManagerTest {
    @MockK
    private lateinit var productApi: ProductApi

    @MockK
    private lateinit var billingManager: BillingManager

    @MockK
    private lateinit var cacheManager: CacheManager

    private lateinit var productManager: ProductManager

    companion object {
        private const val PRODUCTS_CACHE_KEY = "products_cache"
    }

    @Before
    fun setUp() {
        init(this)
        productManager = ProductMangerImpl(productApi, billingManager, cacheManager)
    }

    @Test
    fun `getProducts returns cached data when timestamp is valid`() = runBlocking {
        // Given
        val mockProducts = listOf(
            ProductInfo(
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
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Resource.Success(mockProducts)

        // When
        val result = productManager.getProducts(123L)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(mockProducts, (result as Resource.Success).data)
    }


    @Test
    fun `getProducts filters non-Android products`() = runTest {
        // Given
        val mockProducts = ApiResponse(
            code = 200,
            title = "SUCCESS",
            message = "",
            data = listOf(
                ProductDataDto(
                    productId = "android_product",
                    displayName = "Android Product",
                    description = "Android Description",
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
                ProductDataDto(
                    productId = "ios_product",
                    displayName = "iOS Product",
                    description = "iOS Description",
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
                )
            )
        )

        coEvery { productApi.getProducts() } returns mockProducts

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
        val result = productManager.getProducts(null)

        // Then
        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success<List<ProductInfo>>
        assertEquals(1, successResult.data?.size)
        assertEquals("ANDROID", successResult.data?.first()?.platform)

        // Verify
        coVerify { productApi.getProducts() }
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
                themConfigTimeStamp = 1234567890L
            )
        )

        val mockResponse = Response.success(mockSubscriptionResponse)

        coEvery {
            productApi.getActiveSubscription(userId = any())
        } returns mockResponse

        // When
        val result = productManager.getActiveSubscription("test-user-id")

        // Then
        assertTrue(result is Resource.Success)
        assertNotNull((result as Resource.Success).data)
        assertEquals("test_product", result.data?.subscriptionResponseInfo?.product?.productId)
    }
    @Test
    fun `getProducts returns error when network call fails`() = runTest {
        // Given
        coEvery { productApi.getProducts() } throws Exception("Network error")

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
        val result = productManager.getProducts(null)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `getProducts uses cache when available`() = runTest {
        // Given
        val cachedProducts = listOf(
            ProductInfo(
                productId = "cached_product",
                displayName = "Cached Product",
                description = "Cached Description",
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
        } returns Resource.Success(cachedProducts)

        // When
        val result = productManager.getProducts(123L)

        // Then
        assertTrue(result is Resource.Success)
        assertEquals(cachedProducts, (result as Resource.Success).data)

        // Verify cache was used
        coVerify(exactly = 0) { productApi.getProducts() }
    }

    @Test
    fun `getActiveSubscription returns error on network failure`() = runTest {
        // Given
        coEvery {
            productApi.getActiveSubscription(any(), any())
        } throws Exception("Network error")

        // When
        val result = productManager.getActiveSubscription("test-user")

        // Then
        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }
}