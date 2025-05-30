package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.test.DefaultAsserter.fail

class ProductsApiTest {

    private lateinit var productApi: ProductApi
    private lateinit var mockWebServer: MockWebServer
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val testApiKey = "test-api-key"

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        productApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ProductApi::class.java)
    }

    @Test
    fun `test getActiveSubscription returns successful response with valid data`() {
        // Given
        val mockResponse = """
            {
                "code": 200,
                "title": "SUCCESS",
                "message": "",
                "data": {
                    "subscriptionResponseDTO": {
                        "product": {
                            "id": "test_id",
                            "productId": "aiap_weekly_99",
                            "displayName": "Weekly 99",
                            "description": "Get 50 GB of storage for photos, files  & backup.",
                            "vendorName": "TestVendor",
                            "appName": "IAPApp",
                            "price": 0.99,
                            "displayPrice": "${'$'}0.99",
                            "platform": "ANDROID",
                            "serviceLevel": "CAPSYL_TEST_NA_50G_NA_NA_NA",
                            "isActive": true,
                            "recurringPeriodCode": "P1W",
                            "productType": "SUBSCRIPTION",
                            "entitlementId": null
                        },
                        "vendorName": "TestVendor",
                        "appName": "IAPApp",
                        "appPlatformID": "IAPAppANDROID",
                        "platform": "ANDROID",
                        "partnerUserId": "543a2eb6e-aasd15c-47casd7-94cc-c315551c8413",
                        "startDate": 1736937850340,
                        "endDate": 1736938583245,
                        "status": "Active",
                        "type": "SUBSCRIPTION"
                    },
                    "productUpdateTimeStamp": 1736925150836,
                    "themConfigTimeStamp": 1736921392774,
                    "userUUID": "test-user-uuid",
                    "baseServiceLevel": "basic",
                    "pendingPurchase": false
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { productApi.getActiveSubscription(apiKey = testApiKey, userId = "543a2eb6e-aasd15c-47casd7-94cc-c315551c8413") }

        // Then
        assertNotNull(response.body())
        assertEquals(true, response.isSuccessful)
        val data = response.body()?.data
        assertNotNull(data)
        assertEquals("test-user-uuid", data?.userUUID)
        assertEquals("basic", data?.baseServiceLevel)
        assertFalse(data?.pendingPurchase ?: true)
    }

    @Test
    fun `test getActiveSubscription returns error response`() {
        // Given
        val mockResponse = """
            {
                "code": 404,
                "title": "ERROR",
                "message": "Subscription not found",
                "data": null
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking {
            productApi.getActiveSubscription(apiKey = testApiKey, userId = "invalid-user-id")
        }

        // Then
        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test
    fun `test getProducts returns empty list when no products available`() {
        // Given
        val mockResponse = """
            {
                "code": 200,
                "title": "SUCCESS",
                "message": "",
                "data": []
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { productApi.getProducts(apiKey = testApiKey) }

        // Then
        assertEquals(200, response.code)
        assertTrue(response.data.isEmpty())
    }

    @Test
    fun `test getProducts validates product data fields`() {
        // Given
        val mockResponse = """
            {
                "code": 200,
                "title": "SUCCESS",
                "message": "",
                "data": [{
                    "id": "test_id",
                    "productId": "test_product",
                    "displayName": "Test Product",
                    "description": "Test Description",
                    "vendorName": "TestVendor",
                    "appName": "TestApp",
                    "price": 9.99,
                    "displayPrice": "$9.99",
                    "platform": "ANDROID",
                    "serviceLevel": "TEST_SERVICE",
                    "isActive": true,
                    "recurringPeriodCode": "P1M",
                    "productType": "SUBSCRIPTION",
                    "entitlementId": null
                }]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { productApi.getProducts(apiKey = testApiKey) }

        // Then
        assertEquals(200, response.code)
        assertEquals(1, response.data.size)
        with(response.data[0]) {
            assertEquals("test_product", productId)
            assertEquals("Test Product", displayName)
            assertEquals("TestVendor", vendorName)
            assertEquals(9.99, price, 0.01)
            assertEquals("ANDROID", platform)
            assertTrue(isActive)
        }
    }

    @Test
    fun `test handlePurchase with invalid purchase token`() {
        // Given
        val mockResponse = """
            {
                "code": 400,
                "title": "ERROR",
                "message": "Invalid purchase token",
                "data": null
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(mockResponse)
        )

        val request = HandlePurchaseRequest(
            productId = "test_product",
            purchaseToken = "invalid_token",
            purchaseTime = 1736937850340,
            partnerUserId = "test_user"
        )

        // When
        val response = runBlocking { productApi.handlePurchase(request, apiKey = testApiKey) }

        // Then
        assertNotNull(response)
        assertFalse(response.isSuccessful)
        assertEquals(400, response.code())
    }

    @Test
    fun `test handlePurchase validates request parameters`() {
        // Given
        val mockResponse = """
            {
  "code": 200,
  "title": "SUCCESS",
  "message": "Subscription Updated Successfully",
  "data": {
    "product": {
      "id": "test_id",
      "productId": "test_product",
      "displayName": "Monthly 199",
      "description": "Get 100 GB of storage for photos, files  & backup.",
      "vendorName": "TestVendor",
      "appName": "IAPApp",
      "price": 1.99,
      "displayPrice": "${'$'}1.99",
      "platform": "ANDROID",
      "serviceLevel": "CAPSYL_TEST_NA_100G_NA_NA_NA",
      "isActive": true,
      "recurringPeriodCode": "P1M",
      "productType": "SUBSCRIPTION",
      "entitlementId": null
    },
    "vendorName": "TestVendor",
    "appName": "IAPApp",
    "appPlatformID": "IAPAppANDROID",
    "platform": "ANDROID",
    "partnerUserId": "5432eb6e-a15c-47c7-94cc-c315551c8413",
    "startDate": 1737438749090,
    "endDate": 1737439046272,
    "status": "Active",
    "type": "SUBSCRIPTION",
    "userUUID": "test-user-uuid",
    "baseServiceLevel": "basic"
  }
}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        val request = HandlePurchaseRequest(
            productId = "test_product",
            purchaseToken = "valid_token",
            purchaseTime = System.currentTimeMillis(),
            partnerUserId = "test_user"
        )

        // When
        val response = runBlocking { productApi.handlePurchase(request, apiKey = testApiKey) }

        // Then
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        val responseBody = response.body()
        if (responseBody != null) {
            assertEquals(200, responseBody.code)
            assertEquals("SUCCESS", responseBody.title)
            assertEquals("Subscription Updated Successfully", responseBody.message)
            assertNotNull(responseBody.data)
            assertEquals("test_product", responseBody.data.product.productId)
        } else {
            fail("Response body should not be null")
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}