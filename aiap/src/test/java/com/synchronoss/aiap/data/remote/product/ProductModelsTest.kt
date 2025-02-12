package com.synchronoss.aiap.data.remote.product

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseResponse
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductModelsTest {
    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `test ActiveSubscriptionResponse serialization and deserialization`() {
        val json = """
            {
                "subscriptionResponseDTO": {
                    "product": {
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
                    },
                    "vendorName": "TestVendor",
                    "appName": "TestApp",
                    "appPlatformID": "TestAppAndroid",
                    "platform": "ANDROID",
                    "partnerUserId": "test-user",
                    "startDate": 1234567890,
                    "endDate": 1234567891,
                    "status": "Active",
                    "type": "SUBSCRIPTION"
                },
                "productUpdateTimeStamp": 1234567890,
                "themConfigTimeStamp": 1234567890
            }
        """.trimIndent()

        val adapter = moshi.adapter(ActiveSubscriptionResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.subscriptionResponseDTO)
        assertEquals(1234567890L, response?.productUpdateTimeStamp)
        assertEquals(1234567890L, response?.themConfigTimeStamp)
    }

    @Test
    fun `test HandlePurchaseRequest serialization`() {
        val request = HandlePurchaseRequest(
            productId = "test_product",
            purchaseTime = 1234567890,
            purchaseToken = "test_token",
            partnerUserId = "test_user"
        )

        val adapter = moshi.adapter(HandlePurchaseRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"productId\":\"test_product\""))
        assertTrue(json.contains("\"purchaseTime\":1234567890"))
        assertTrue(json.contains("\"purchaseToken\":\"test_token\""))
        assertTrue(json.contains("\"partnerUserId\":\"test_user\""))
    }

    @Test
    fun `test HandlePurchaseResponse deserialization`() {
        val json = """
            {
    "product": {
      "productId": "test_product",
      "displayName": "test product",
      "description": "Get 100 GB of storage for photos, files  & backup.",
      "vendorName": "TestVendor",
      "appName": "test app",
      "price": 1.99,
      "displayPrice": "${'$'}1.99",
      "platform": "ANDROID",
      "serviceLevel": "test service",
      "isActive": true,
      "recurringPeriodCode": "P1M",
      "productType": "SUBSCRIPTION",
      "entitlementId": null
    },
    "vendorName": "TestVendor",
    "appName": "TestApp",
    "appPlatformID": "TestAppAndroid",
    "platform": "ANDROID",
    "partnerUserId": "test-user",
    "startDate": 1234567890,
    "endDate": 1234567891,
    "status": "Active",
    "type": "SUBSCRIPTION"
  }
        """.trimIndent()

        val adapter = moshi.adapter(HandlePurchaseResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("test_product", response?.product?.productId)
        assertEquals("TestVendor", response?.vendorName)
        assertEquals("TestApp", response?.appName)
        assertEquals("TestAppAndroid", response?.appPlatformID)
        assertEquals("ANDROID", response?.platform)
        assertEquals("test-user", response?.partnerUserId)
        assertEquals(1234567890L, response?.startDate)
        assertEquals(1234567891L, response?.endDate)
        assertEquals("Active", response?.status)
        assertEquals("SUBSCRIPTION", response?.type)
    }

    @Test
    fun `test ProductDataDto serialization and deserialization`() {
        val json = """
            {
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
            }
        """.trimIndent()

        val adapter = moshi.adapter(ProductDataDto::class.java)
        val product = adapter.fromJson(json)

        assertNotNull(product)
        assertEquals("test_product", product?.productId)
        assertEquals("Test Product", product?.displayName)
        assertEquals("Test Description", product?.description)
        assertEquals("TestVendor", product?.vendorName)
        assertEquals("TestApp", product?.appName)
        assertEquals(9.99, product?.price)
        assertEquals("$9.99", product?.displayPrice)
        assertEquals("ANDROID", product?.platform)
        assertEquals("TEST_SERVICE", product?.serviceLevel)
        assertTrue(product?.isActive == true)
        assertEquals("P1M", product?.recurringPeriodCode)
        assertEquals("SUBSCRIPTION", product?.productType)
        assertNull(product?.entitlementId)
    }

    @Test
    fun `test SubscriptionResponseDTO serialization and deserialization`() {
        val json = """
            {
                "product": {
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
                },
                "vendorName": "TestVendor",
                "appName": "TestApp",
                "appPlatformID": "TestAppAndroid",
                "platform": "ANDROID",
                "partnerUserId": "test-user",
                "startDate": 1234567890,
                "endDate": 1234567891,
                "status": "Active",
                "type": "SUBSCRIPTION"
            }
        """.trimIndent()

        val adapter = moshi.adapter(SubscriptionResponseDTO::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.product)
        assertEquals("TestVendor", response?.vendorName)
        assertEquals("TestApp", response?.appName)
        assertEquals("TestAppAndroid", response?.appPlatformID)
        assertEquals("ANDROID", response?.platform)
        assertEquals("test-user", response?.partnerUserId)
        assertEquals(1234567890L, response?.startDate)
        assertEquals(1234567891L, response?.endDate)
        assertEquals("Active", response?.status)
        assertEquals("SUBSCRIPTION", response?.type)
    }

    @Test
    fun `test null handling in ActiveSubscriptionResponse`() {
        val json = """
            {
                "subscriptionResponseDTO": null,
                "productUpdateTimeStamp": null,
                "themConfigTimeStamp": null
            }
        """.trimIndent()

        val adapter = moshi.adapter(ActiveSubscriptionResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNull(response?.subscriptionResponseDTO)
        assertNull(response?.productUpdateTimeStamp)
        assertNull(response?.themConfigTimeStamp)
    }

    @Test
    fun `test null product in SubscriptionResponseDTO`() {
        val json = """
            {
                "product": null,
                "vendorName": "TestVendor",
                "appName": "TestApp",
                "appPlatformID": "TestAppAndroid",
                "platform": "ANDROID",
                "partnerUserId": "test-user",
                "startDate": 1234567890,
                "endDate": 1234567891,
                "status": "Active",
                "type": "SUBSCRIPTION"
            }
        """.trimIndent()

        val adapter = moshi.adapter(SubscriptionResponseDTO::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNull(response?.product)
        assertEquals("TestVendor", response?.vendorName)
    }
}