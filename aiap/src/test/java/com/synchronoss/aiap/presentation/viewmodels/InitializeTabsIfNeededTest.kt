package com.synchronoss.aiap.presentation.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.activity.ComponentActivity
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.analytics.AnalyticsUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.presentation.subscriptions.ui.TabOption
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.NetworkConnectionListener
import com.synchronoss.aiap.utils.ToastService
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * This test class focuses specifically on testing the initializeTabsIfNeeded method
 * to improve its coverage.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InitializeTabsIfNeededTest {
    private lateinit var viewModel: SubscriptionsViewModel
    private lateinit var context: Context
    private lateinit var billingManagerUseCases: BillingManagerUseCases
    private lateinit var productManagerUseCases: ProductManagerUseCases
    private lateinit var themeLoader: ThemeLoader
    private lateinit var libraryActivityManagerUseCases: LibraryActivityManagerUseCases
    private lateinit var purchaseUpdateHandler: PurchaseUpdateHandler
    private lateinit var subscriptionCancelledHandler: SubscriptionCancelledHandler
    private lateinit var analyticsUseCases: AnalyticsUseCases
    private lateinit var networkConnectionListener: NetworkConnectionListener
    private lateinit var toastService: ToastService
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock Log calls
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        
        // Initialize mocks with relaxed behavior
        context = mockk(relaxed = true)
        billingManagerUseCases = mockk(relaxed = true)
        productManagerUseCases = mockk(relaxed = true)
        themeLoader = mockk(relaxed = true)
        libraryActivityManagerUseCases = mockk(relaxed = true)
        purchaseUpdateHandler = mockk(relaxed = true)
        subscriptionCancelledHandler = mockk(relaxed = true)
        analyticsUseCases = mockk(relaxed = true)
        
        // Mock ConnectivityManager for NetworkConnectionListener
        val mockConnectivityManager = mockk<ConnectivityManager>(relaxed = true)
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        
        // Mock NetworkCapabilities
        val mockNetworkCapabilities = mockk<NetworkCapabilities>(relaxed = true)
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(any()) } returns true
        
        // Create toast service manually for testing
        toastService = ToastService(context, testScope)
        
        // Create and mock NetworkConnectionListener
        networkConnectionListener = mockk(relaxed = true)
        
        // Create the viewModel by spying on the original implementation
        viewModel = spyk(
            SubscriptionsViewModel(
                billingManagerUseCases = billingManagerUseCases,
                productManagerUseCases = productManagerUseCases,
                themeLoader = themeLoader,
                libraryActivityManagerUseCases = libraryActivityManagerUseCases,
                purchaseUpdateHandler = purchaseUpdateHandler,
                subscriptionCancelledHandler = subscriptionCancelledHandler,
                analyticsUseCases = analyticsUseCases,
                context = context
            ), 
            recordPrivateCalls = true
        )
        
        // Replace internal properties with our mocked ones using reflection
        val toastServiceField = SubscriptionsViewModel::class.java.getDeclaredField("toastService")
        toastServiceField.isAccessible = true
        toastServiceField.set(viewModel, toastService)
        
        val networkListenerField = SubscriptionsViewModel::class.java.getDeclaredField("networkConnectionListener")
        networkListenerField.isAccessible = true
        networkListenerField.set(viewModel, networkConnectionListener)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Helper method to create a mock ProductDetails with specific billing period
     * that includes all necessary properties for the test.
     */
    private fun createMockProductDetails(productId: String, billingPeriod: String): ProductDetails {
        val mockProductDetails = mockk<ProductDetails>()
        every { mockProductDetails.productId } returns productId
        every { mockProductDetails.name } returns "Test $productId"
        
        val mockOfferDetails = mockk<ProductDetails.SubscriptionOfferDetails>()
        val mockPricingPhases = mockk<ProductDetails.PricingPhases>()
        val mockPricingPhase = mockk<ProductDetails.PricingPhase>()
        
        every { mockPricingPhase.billingPeriod } returns billingPeriod
        every { mockPricingPhase.formattedPrice } returns "$9.99"
        every { mockPricingPhase.priceAmountMicros } returns 9990000L
        
        every { mockPricingPhases.pricingPhaseList } returns listOf(mockPricingPhase)
        every { mockOfferDetails.pricingPhases } returns mockPricingPhases
        every { mockOfferDetails.offerToken } returns "offer-token-$productId"
        
        every { mockProductDetails.subscriptionOfferDetails } returns listOf(mockOfferDetails)
        
        return mockProductDetails
    }

    @Test
    fun `test initializeTabsIfNeeded with null selectedTab and null productDetails defaults to YEARLY`() = runTest {
        // Given
        viewModel.selectedTab = null
        viewModel.productDetails = null
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
        assertNull(viewModel.filteredProductDetails)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with existing selectedTab does nothing`() = runTest {
        // Given
        viewModel.selectedTab = TabOption.MONTHLY
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with empty product list defaults to YEARLY`() = runTest {
        // Given
        viewModel.selectedTab = null
        viewModel.productDetails = emptyList()
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
    }

    @Test
    fun `test initializeTabsIfNeeded with currentProductDetails having weekly billing period`() = runTest {
        // Given - Current product with weekly billing period
        val weeklyProductDetails = createMockProductDetails("weekly_product", "P1W")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = weeklyProductDetails
        viewModel.productDetails = listOf(weeklyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.WEEKlY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with currentProductDetails having monthly billing period`() = runTest {
        // Given - Current product with monthly billing period
        val monthlyProductDetails = createMockProductDetails("monthly_product", "P1M")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = monthlyProductDetails
        viewModel.productDetails = listOf(monthlyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with currentProductDetails having yearly billing period`() = runTest {
        // Given - Current product with yearly billing period
        val yearlyProductDetails = createMockProductDetails("yearly_product", "P1Y")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = yearlyProductDetails
        viewModel.productDetails = listOf(yearlyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with currentProductDetails but unknown billing period`() = runTest {
        // Given - Current product with unknown billing period
        val unknownPeriodProductDetails = createMockProductDetails("unknown_product", "UNKNOWN")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = unknownPeriodProductDetails
        viewModel.productDetails = listOf(unknownPeriodProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then - Default to YEARLY for unknown period
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with multiple product types prioritizes weekly plans`() = runTest {
        // Given - Multiple product types available
        val weeklyProductDetails = createMockProductDetails("weekly_product", "P1W")
        val monthlyProductDetails = createMockProductDetails("monthly_product", "P1M")
        val yearlyProductDetails = createMockProductDetails("yearly_product", "P1Y")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = null  // No current product
        viewModel.productDetails = listOf(weeklyProductDetails, monthlyProductDetails, yearlyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then - Should select WEEKLY since weekly products are available
        assertEquals(TabOption.WEEKlY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with only monthly and yearly products prioritizes monthly`() = runTest {
        // Given - Monthly and yearly products, but no weekly
        val monthlyProductDetails = createMockProductDetails("monthly_product", "P1M")
        val yearlyProductDetails = createMockProductDetails("yearly_product", "P1Y")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = null
        viewModel.productDetails = listOf(monthlyProductDetails, yearlyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then - Should select MONTHLY since no weekly products are available
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with only yearly products selects yearly`() = runTest {
        // Given - Only yearly products available
        val yearlyProductDetails = createMockProductDetails("yearly_product", "P1Y")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = null
        viewModel.productDetails = listOf(yearlyProductDetails)
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then - Should select YEARLY since only yearly products are available
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
        assertNotNull(viewModel.filteredProductDetails)
        assertEquals(1, viewModel.filteredProductDetails?.size)
    }
    
    @Test
    fun `test initializeTabsIfNeeded with null productDetails but non-null currentProductDetails`() = runTest {
        // Given - Current product with yearly billing period but null productDetails list
        val yearlyProductDetails = createMockProductDetails("yearly_product", "P1Y")
        
        viewModel.selectedTab = null
        viewModel.currentProductDetails = yearlyProductDetails
        viewModel.productDetails = null
        
        // When
        viewModel.initializeTabsIfNeeded()
        
        // Then - Should set tab based on current product's billing period
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
        assertNull(viewModel.filteredProductDetails)
    }

    @Test
    fun `test initializeTabsIfNeeded comprehensive coverage of edge cases`() = runTest {
        // Spy on the method implementation to see what branches are executed
        val spyViewModel = spyk(viewModel)
        
        // Create weekly product
        val weeklyProduct = createMockProductDetails("weekly", "P1W")
        val monthlyProduct = createMockProductDetails("monthly", "P1M")
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        
        // First scenario: basic test with weekly product only
        spyViewModel.selectedTab = null
        spyViewModel.currentProductDetails = null
        spyViewModel.productDetails = listOf(weeklyProduct)
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 1 - Weekly only - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.WEEKlY, spyViewModel.selectedTab)
        
        // Reset
        spyViewModel.selectedTab = null
        
        // Second scenario: basic test with monthly product only
        spyViewModel.productDetails = listOf(monthlyProduct)
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 2 - Monthly only - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.MONTHLY, spyViewModel.selectedTab)
        
        // Reset
        spyViewModel.selectedTab = null
        
        // Third scenario: basic test with yearly product only
        spyViewModel.productDetails = listOf(yearlyProduct)
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 3 - Yearly only - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.YEARLY, spyViewModel.selectedTab)
        
        // Reset
        spyViewModel.selectedTab = null
        
        // Fourth scenario: null product details, should default to yearly
        spyViewModel.productDetails = null
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 4 - Null products - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.YEARLY, spyViewModel.selectedTab)
        
        // Reset
        spyViewModel.selectedTab = null
        
        // Fifth scenario: empty product details, should default to yearly
        spyViewModel.productDetails = emptyList()
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 5 - Empty products - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.YEARLY, spyViewModel.selectedTab)
        
        // Reset 
        spyViewModel.selectedTab = null
        
        // Sixth scenario: selected tab already set, should not change
        spyViewModel.selectedTab = TabOption.MONTHLY
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 6 - Tab already set - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.MONTHLY, spyViewModel.selectedTab)
        
        // Reset
        spyViewModel.selectedTab = null
        
        // Seventh scenario: mix of products, should prioritize weekly
        spyViewModel.productDetails = listOf(weeklyProduct, monthlyProduct, yearlyProduct)
        spyViewModel.initializeTabsIfNeeded()
        
        println("Test 7 - Mix of products - Selected tab: ${spyViewModel.selectedTab}")
        assertEquals(TabOption.WEEKlY, spyViewModel.selectedTab)
    }
} 