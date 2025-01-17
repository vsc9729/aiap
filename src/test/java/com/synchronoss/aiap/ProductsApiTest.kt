package com.synchronoss.aiap

import com.google.android.gms.common.api.Response
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.data.remote.product.ProductApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ProductsApiTest {

    private lateinit var productApi: ProductApi
    private lateinit var mockWebServer: MockWebServer
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

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
    fun `test getActiveSubscription returns successful response`() {
        // Given
        val mockResponse = """
            {
    "code": 200,
    "title": "SUCCESS",
    "message": "",
    "data": {
        "subscriptionResponseDTO": {
            "product": {
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
        "themConfigTimeStamp": 1736921392774
    }
}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { productApi.getActiveSubscription() }

        // Then
        assertNotNull(response.body())
        assertEquals(true, response.isSuccessful)
    }

    @Test
    fun `test getProducts returns successful response`() {
        // Given
        val mockResponse = """
            {
                "status": "SUCCESS",
                "data": [
                    {
                        "productId": "test_product",
                        "displayName": "Test Product",
                        "price": 99.9,
                        "isActive": true
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // When
        val response = runBlocking { productApi.getProducts() }

        // Then
        assertEquals("SUCCESS", response.status)
        assertEquals(1, response.data?.size)
        assertEquals("test_product", response.data?.first()?.productId)
    }

    @Test
    fun `test handlePurchase returns successful response`() {
        // Given
        val mockResponse = """
            {
                "status": "SUCCESS",
                "data": {
                    "message": "Purchase successful",
                    "orderId": "test_order_123"
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
            purchaseToken = "test_token"
        )

        // When
        val response = runBlocking { productApi.handlePurchase(request) }

        // Then
        assertNotNull(response.body())
        assertEquals("SUCCESS", response.body()?.status)
        assertEquals("test_order_123", response.body()?.data?.orderId)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}