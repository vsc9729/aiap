package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.core.data.mappers.toActiveSubscriptionInfo
import com.synchronoss.aiap.core.data.mappers.toProductInfo
import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import com.synchronoss.aiap.core.domain.models.ProductInfo
import org.junit.Assert.*
import org.junit.Test

class ProductMappersTest {

    @Test
    fun `test product data to product info mapping`() {
        // Prepare sample data
        val productDataDto = ProductDataDto(
            productId = "123", displayName = "Test Product", description = "Description",
            vendorName = "Vendor", appName = "App", price = 9.99, displayPrice = "$9.99",
            platform = "Android", serviceLevel = "Basic", isActive = true,
            recurringPeriodCode = "MONTHLY", productType = "Subscription", entitlementId = "ent123",
            id = "1"
        )

        // Map to ProductInfo
        val productInfo = productDataDto.toProductInfo()

        // Validate mapping
        assertEquals("123", productInfo.productId)
        assertEquals("Test Product", productInfo.displayName)
        assertEquals("Description", productInfo.description)
        assertEquals("Vendor", productInfo.vendorName)
        assertEquals("App", productInfo.appName)
        assertEquals(9.99, productInfo.price, 0.0)
        assertEquals("$9.99", productInfo.displayPrice)
        assertEquals("Android", productInfo.platform)
        assertEquals("Basic", productInfo.serviceLevel)
        assertTrue(productInfo.isActive)
        assertEquals("MONTHLY", productInfo.recurringPeriodCode)
        assertEquals("Subscription", productInfo.productType)
        assertEquals("ent123", productInfo.entitlementId)
    }

    @Test
    fun `test active subscription response mapping`() {
        // Prepare sample data
        val subscriptionResponseDTO = SubscriptionResponseDTO(
            product = ProductDataDto(
                productId = "123", displayName = "Test Product", description = "Description",
                vendorName = "Vendor", appName = "App", price = 9.99, displayPrice = "$9.99",
                platform = "Android", serviceLevel = "Basic", isActive = true,
                recurringPeriodCode = "MONTHLY", productType = "Subscription", entitlementId = "ent123",
                id = "1"
            ),
            vendorName = "Vendor", appName = "App", appPlatformID = "1", platform = "Android",
            partnerUserId = "user123", startDate = 123L, endDate = 124L, status = "Active", type = "Subscription"
        )
        val activeSubscriptionResponse = ActiveSubscriptionResponse(
            subscriptionResponseDTO = subscriptionResponseDTO,
            productUpdateTimeStamp = 123L,
            themConfigTimeStamp = 123L,
            userUUID =  "1"
        )

        // Map to ActiveSubscriptionInfo
        val activeSubscriptionInfo = activeSubscriptionResponse.toActiveSubscriptionInfo()

        // Validate mapping
        assertNotNull(activeSubscriptionInfo.subscriptionResponseInfo)
        assertEquals("123", activeSubscriptionInfo.subscriptionResponseInfo?.product?.productId)
        assertEquals("Test Product", activeSubscriptionInfo.subscriptionResponseInfo?.product?.displayName)
        assertEquals("Description", activeSubscriptionInfo.subscriptionResponseInfo?.product?.description)
        assertEquals("Vendor", activeSubscriptionInfo.subscriptionResponseInfo?.product?.vendorName)
        assertEquals("App", activeSubscriptionInfo.subscriptionResponseInfo?.product?.appName)
        assertEquals(9.99, activeSubscriptionInfo.subscriptionResponseInfo?.product?.price?:0.0, 0.0)
        assertEquals("$9.99", activeSubscriptionInfo.subscriptionResponseInfo?.product?.displayPrice)
        assertEquals("Android", activeSubscriptionInfo.subscriptionResponseInfo?.product?.platform)
        assertEquals("Basic", activeSubscriptionInfo.subscriptionResponseInfo?.product?.serviceLevel)
        assertTrue(activeSubscriptionInfo.subscriptionResponseInfo?.product?.isActive ?: false)
        assertEquals("MONTHLY", activeSubscriptionInfo.subscriptionResponseInfo?.product?.recurringPeriodCode)
        assertEquals("Subscription", activeSubscriptionInfo.subscriptionResponseInfo?.product?.productType)
        assertEquals("ent123", activeSubscriptionInfo.subscriptionResponseInfo?.product?.entitlementId)
        assertEquals("Vendor", activeSubscriptionInfo.subscriptionResponseInfo?.vendorName)
        assertEquals("App", activeSubscriptionInfo.subscriptionResponseInfo?.appName)
        assertEquals("1", activeSubscriptionInfo.subscriptionResponseInfo?.appPlatformID)
        assertEquals("Android", activeSubscriptionInfo.subscriptionResponseInfo?.platform)
        assertEquals("user123", activeSubscriptionInfo.subscriptionResponseInfo?.partnerUserId)
        assertEquals(123L, activeSubscriptionInfo.subscriptionResponseInfo?.startDate)
        assertEquals(124L, activeSubscriptionInfo.subscriptionResponseInfo?.endDate)
        assertEquals("Active", activeSubscriptionInfo.subscriptionResponseInfo?.status)
        assertEquals("Subscription", activeSubscriptionInfo.subscriptionResponseInfo?.type)
        assertEquals(123L, activeSubscriptionInfo.productUpdateTimeStamp)
        assertEquals(123L, activeSubscriptionInfo.themConfigTimeStamp)
    }
}