package com.synchronoss.aiap.core.domain.usecases.billing

import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BillingManagerUseCasesTest {

    @Test
    fun `test BillingManagerUseCases constructor properly initializes all properties`() {
        // Given
        val mockStartConnection = mockk<StartConnection>()
        val mockPurchaseSubscription = mockk<PurchaseSubscription>()
        val mockCheckExistingSubscription = mockk<CheckExistingSubscription>()
        val mockHandleUnacknowledgedPurchases = mockk<HandleUnacknowledgedPurchases>()
        val mockGetProductDetails = mockk<GetProductDetails>()
        
        // When
        val useCases = BillingManagerUseCases(
            startConnection = mockStartConnection,
            purchaseSubscription = mockPurchaseSubscription,
            checkExistingSubscription = mockCheckExistingSubscription,
            handleUnacknowledgedPurchases = mockHandleUnacknowledgedPurchases,
            getProductDetails = mockGetProductDetails
        )
        
        // Then
        assertNotNull(useCases)
        assertEquals(mockStartConnection, useCases.startConnection, "StartConnection use case should be initialized correctly")
        assertEquals(mockPurchaseSubscription, useCases.purchaseSubscription, "PurchaseSubscription use case should be initialized correctly")
        assertEquals(mockCheckExistingSubscription, useCases.checkExistingSubscription, "CheckExistingSubscription use case should be initialized correctly")
        assertEquals(mockHandleUnacknowledgedPurchases, useCases.handleUnacknowledgedPurchases, "HandleUnacknowledgedPurchases use case should be initialized correctly")
        assertEquals(mockGetProductDetails, useCases.getProductDetails, "GetProductDetails use case should be initialized correctly")
    }
}