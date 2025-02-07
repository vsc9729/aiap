package com.synchronoss.aiap.presentation

import android.util.Log
import com.synchronoss.aiap.di.PurchaseUpdateHandler
import com.synchronoss.aiap.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.ui.theme.ThemeLoader
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionsViewTest {
    private lateinit var viewModel: SubscriptionsViewModel
    private lateinit var billingManagerUseCases: BillingManagerUseCases
    private lateinit var productManagerUseCases: ProductManagerUseCases
    private lateinit var themeLoader: ThemeLoader
    private lateinit var libraryActivityManagerUseCases: LibraryActivityManagerUseCases
    private lateinit var purchaseUpdateHandler: PurchaseUpdateHandler
    private lateinit var subscriptionCancelledHandler: SubscriptionCancelledHandler
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        billingManagerUseCases = mockk(relaxed = true)
        productManagerUseCases = mockk(relaxed = true)
        themeLoader = mockk(relaxed = true)
        libraryActivityManagerUseCases = mockk(relaxed = true)
        purchaseUpdateHandler = mockk(relaxed = true)
        subscriptionCancelledHandler = mockk(relaxed = true)

        viewModel = SubscriptionsViewModel(
            billingManagerUseCases,
            productManagerUseCases,
            themeLoader,
            libraryActivityManagerUseCases,
            purchaseUpdateHandler,
            subscriptionCancelledHandler
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test tab selection filters products correctly`() = runTest {
        // Given
        val mockProducts = listOf(
            createMockProduct("1", "P1M"),
            createMockProduct("2", "P1Y"),
            createMockProduct("3", "P1W")
        )
        viewModel.products = mockProducts

        // When - Select Monthly
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(1, viewModel.filteredProducts?.size)
        assertEquals("P1M", viewModel.filteredProducts?.first()?.recurringPeriodCode)

        // When - Select Yearly
        viewModel.onTabSelected(TabOption.YEARLY)

        // Then
        assertEquals(1, viewModel.filteredProducts?.size)
        assertEquals("P1Y", viewModel.filteredProducts?.first()?.recurringPeriodCode)
    }

    @Test
    fun `test plan selection updates selected plan index`() = runTest {
        // Given
        val mockProducts = listOf(
            createMockProduct("1", "P1M"),
            createMockProduct("2", "P1Y")
        )
        viewModel.products = mockProducts
        viewModel.onTabSelected(TabOption.MONTHLY)

        // When
        viewModel.selectedPlan = 0

        // Then
        assertEquals(0, viewModel.selectedPlan)
        assertEquals(mockProducts[0], viewModel.filteredProducts?.first())
    }

    @Test
    fun `test no internet connection state`() = runTest {
        // Given
        viewModel.noInternetConnectionAndNoCache.value = true

        // Then
        assertTrue(viewModel.noInternetConnectionAndNoCache.value)
    }

    private fun createMockProduct(id: String, recurringPeriodCode: String) = ProductInfo(
        productId = id,
        displayName = "Test Product $id",
        description = "Description",
        vendorName = "Vendor",
        appName = "App",
        price = 9.99,
        displayPrice = "$9.99",
        platform = "ANDROID",
        serviceLevel = "Basic",
        isActive = true,
        recurringPeriodCode = recurringPeriodCode,
        productType = "Subscription",
        entitlementId = null
    )
}