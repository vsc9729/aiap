package com.synchronoss.aiap.data.remote.product

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
        val response = runBlocking { productApi.getActiveSubscription(userId = "543a2eb6e-aasd15c-47casd7-94cc-c315551c8413") }

        // Then
        assertNotNull(response.body())
        assertEquals(true, response.isSuccessful)
    }

    @Test
    fun `test getProducts returns successful response`() {
        // Given
        val mockResponse = """
            {
  "code": 200,
  "title": "SUCCESS",
  "message": "",
  "data": [
    {
      "productId": "aiap_yearly_999",
      "displayName": "Yearly 999",
      "description": "Get 250 GB of storage for photos, files  & backup.",
      "vendorName": "TestVendor",
      "appName": "IAPApp",
      "price": 99.9,
      "displayPrice": "${'$'}99.9",
      "platform": "ANDROID",
      "serviceLevel": "CAPSYL_TEST_NA_250G_NA_NA_NA",
      "isActive": true,
      "recurringPeriodCode": "P1Y",
      "productType": "SUBSCRIPTION",
      "entitlementId": null
    },
    {
      "productId": "aiap_monthly_199",
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
    {
      "productId": "aiap_50GB",
      "displayName": "Testing 50GB Product",
      "description": "Testing Product",
      "vendorName": "TestVendor",
      "appName": "IAPApp",
      "price": 199,
      "displayPrice": "${'$'}199.0",
      "platform": "ANDROID",
      "serviceLevel": "CAPSYL_TEST_NA_50G_NA_NA_NA",
      "isActive": true,
      "recurringPeriodCode": "P1Y",
      "productType": "SUBSCRIPTION",
      "entitlementId": null
    },
    {
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
        assertEquals(200, response.code)
        assertEquals(4, response.data.size)
    }

    @Test
    fun `test handlePurchase returns successful response`() {
        // Given
        val mockResponse = """
            {
  "code": 200,
  "title": "SUCCESS",
  "message": "Subscription Updated Successfully",
  "data": {
    "productId": "aiap_yearly_999",
    "serviceLevel": "CAPSYL_TEST_NA_250G_NA_NA_NA",
    "vendorName": "TestVendor",
    "appName": "IAPApp",
    "appPlatformID": "IAPAppANDROID",
    "platform": "ANDROID",
    "partnerUserId": "5432eb6e-a15c-47c7-94cc-c315551c8413",
    "startDate": 1737358159992,
    "endDate": 1737359957230,
    "status": "Active",
    "type": "SUBSCRIPTION"
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
            purchaseToken = "test_token",
            purchaseTime = 1736937850340,
            partnerUserId = "543a2eb6e-aasd15c-47casd7-94cc-c315551c8413"
        )

        // When
        val response = runBlocking { productApi.handlePurchase(request) }

        // Then
        assertNotNull(response.body())
        assertEquals(200, response.body()!!.code)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}