package com.synchronoss.aiap.core.data.mappers

import com.synchronoss.aiap.core.data.remote.product.ActiveSubscriptionResponse
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.data.remote.product.SubscriptionResponseDTO
import org.junit.Assert.*
import org.junit.Test

class DtoExtTest {

    @Test
    fun `test product data to product info mapping`() {
        // Prepare sample data
        val productDataDto = ProductDataDto(
            id = "1",
            productId = "123",
            displayName = "Test Product",
            description = "Description",
            vendorName = "Vendor",
            appName = "App",
            price = 9.99,
            displayPrice = "$9.99",
            platform = "Android",
            serviceLevel = "Basic",
            isActive = true,
            recurringPeriodCode = "MONTHLY",
            productType = "Subscription",
            entitlementId = "ent123"
        )

        // Map to ProductInfo
        val productInfo = productDataDto.toProductInfo()

        // Validate mapping
        assertEquals("1", productInfo.id)
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
                id = "1",
                productId = "123", 
                displayName = "Test Product", 
                description = "Description",
                vendorName = "Vendor", 
                appName = "App", 
                price = 9.99, 
                displayPrice = "$9.99",
                platform = "Android", 
                serviceLevel = "Basic", 
                isActive = true,
                recurringPeriodCode = "MONTHLY", 
                productType = "Subscription", 
                entitlementId = "ent123"
            ),
            vendorName = "Vendor",
            appName = "App",
            appPlatformID = "appID",
            platform = "Android",
            partnerUserId = "user123",
            startDate = 1000L,
            endDate = 2000L,
            status = "Active",
            type = "Subscription"
        )

        val response = ActiveSubscriptionResponse(
            subscriptionResponseDTO = subscriptionResponseDTO,
            productUpdateTimeStamp = 1234L,
            themConfigTimeStamp = 5678L,
            userUUID = "uuid123",
            baseServiceLevel = "Basic",
            pendingPurchase = false
        )

        // Map to domain model
        val activeSubscriptionInfo = response.toActiveSubscriptionInfo()

        // Validate mapping
        assertNotNull(activeSubscriptionInfo.subscriptionResponseInfo)
        assertEquals(1234L, activeSubscriptionInfo.productUpdateTimeStamp)
        assertEquals(5678L, activeSubscriptionInfo.themConfigTimeStamp)
        assertEquals("uuid123", activeSubscriptionInfo.userUUID)
        assertEquals("Basic", activeSubscriptionInfo.baseServiceLevel)
        assertFalse(activeSubscriptionInfo.pendingPurchase)
        
        val subscriptionResponseInfo = activeSubscriptionInfo.subscriptionResponseInfo
        if (subscriptionResponseInfo != null) {
            assertEquals("Vendor", subscriptionResponseInfo.vendorName)
            assertEquals("App", subscriptionResponseInfo.appName)
            assertEquals("appID", subscriptionResponseInfo.appPlatformID)
            assertEquals("Android", subscriptionResponseInfo.platform)
            assertEquals("user123", subscriptionResponseInfo.partnerUserId)
            assertEquals(1000L, subscriptionResponseInfo.startDate)
            assertEquals(2000L, subscriptionResponseInfo.endDate)
            assertEquals("Active", subscriptionResponseInfo.status)
            assertEquals("Subscription", subscriptionResponseInfo.type)
        } else {
            fail("subscriptionResponseInfo should not be null")
        }
    }

    @Test
    fun `test subscription response dto to subscription response info mapping`() {
        // Prepare sample data
        val subscriptionResponseDTO = SubscriptionResponseDTO(
            product = ProductDataDto(
                id = "1",
                productId = "123", 
                displayName = "Test Product", 
                description = "Description",
                vendorName = "Vendor", 
                appName = "App", 
                price = 9.99, 
                displayPrice = "$9.99",
                platform = "Android", 
                serviceLevel = "Basic", 
                isActive = true,
                recurringPeriodCode = "MONTHLY", 
                productType = "Subscription", 
                entitlementId = "ent123"
            ),
            vendorName = "Vendor",
            appName = "App",
            appPlatformID = "appID",
            platform = "Android",
            partnerUserId = "user123",
            startDate = 1000L,
            endDate = 2000L,
            status = "Active",
            type = "Subscription"
        )

        // Map to SubscriptionResponseInfo
        val subscriptionResponseInfo = subscriptionResponseDTO.toSubscriptionResponseInfo()

        // Validate mapping
        assertNotNull(subscriptionResponseInfo.product)
        val productInfo = subscriptionResponseInfo.product
        if (productInfo != null) {
            assertEquals("1", productInfo.id)
            assertEquals("123", productInfo.productId)
            assertEquals("Test Product", productInfo.displayName)
            assertEquals("Description", productInfo.description)
        } else {
            fail("productInfo should not be null")
        }
        assertEquals("Vendor", subscriptionResponseInfo.vendorName)
        assertEquals("App", subscriptionResponseInfo.appName)
        assertEquals("appID", subscriptionResponseInfo.appPlatformID)
        assertEquals("Android", subscriptionResponseInfo.platform)
        assertEquals("user123", subscriptionResponseInfo.partnerUserId)
        assertEquals(1000L, subscriptionResponseInfo.startDate)
        assertEquals(2000L, subscriptionResponseInfo.endDate)
        assertEquals("Active", subscriptionResponseInfo.status)
        assertEquals("Subscription", subscriptionResponseInfo.type)
    }
}