package com.synchronoss.aiap.presentation.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OnTabSelectedTest {
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
        
        // Initialize mocks
        context = mockk(relaxed = true)
        billingManagerUseCases = mockk(relaxed = true)
        productManagerUseCases = mockk(relaxed = true)
        themeLoader = mockk(relaxed = true)
        libraryActivityManagerUseCases = mockk(relaxed = true)
        purchaseUpdateHandler = mockk(relaxed = true)
        subscriptionCancelledHandler = mockk(relaxed = true)
        analyticsUseCases = mockk(relaxed = true)
        
        // Mock ConnectivityManager
        val mockConnectivityManager = mockk<ConnectivityManager>(relaxed = true)
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        
        // Mock NetworkCapabilities
        val mockNetworkCapabilities = mockk<NetworkCapabilities>(relaxed = true)
        every { mockConnectivityManager.activeNetwork } returns mockk()
        every { mockConnectivityManager.getNetworkCapabilities(any()) } returns mockNetworkCapabilities
        every { mockNetworkCapabilities.hasTransport(any()) } returns true
        
        // Create toast service
        toastService = ToastService(context, testScope)
        
        // Create NetworkConnectionListener
        networkConnectionListener = mockk(relaxed = true)
        
        // Create ViewModel
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
            )
        )
        
        // Set internal properties
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
    fun `test onTabSelected with null tab`() {
        // Given
        viewModel.selectedPlan = 1
        viewModel.selectedTab = TabOption.MONTHLY
        viewModel.productDetails = listOf(
            createMockProductDetails("monthly", "P1M"),
            createMockProductDetails("yearly", "P1Y")
        )

        // When
        viewModel.onTabSelected(null)

        // Then
        assertEquals(-1, viewModel.selectedPlan)
        assertNull(viewModel.selectedTab)
        assertNull(viewModel.filteredProductDetails)
    }

    @Test
    fun `test onTabSelected with MONTHLY tab`() {
        // Given
        val monthlyProduct = createMockProductDetails("monthly", "P1M")
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        viewModel.productDetails = listOf(monthlyProduct, yearlyProduct)

        // When
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(1, viewModel.filteredProductDetails?.size)
        assertEquals("monthly", viewModel.filteredProductDetails?.first()?.productId)
    }

    @Test
    fun `test onTabSelected with YEARLY tab`() {
        // Given
        val monthlyProduct = createMockProductDetails("monthly", "P1M")
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        viewModel.productDetails = listOf(monthlyProduct, yearlyProduct)

        // When
        viewModel.onTabSelected(TabOption.YEARLY)

        // Then
        assertEquals(TabOption.YEARLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(1, viewModel.filteredProductDetails?.size)
        assertEquals("yearly", viewModel.filteredProductDetails?.first()?.productId)
    }

    @Test
    fun `test onTabSelected with WEEKLY tab`() {
        // Given
        val weeklyProduct = createMockProductDetails("weekly", "P1W")
        val monthlyProduct = createMockProductDetails("monthly", "P1M")
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        viewModel.productDetails = listOf(weeklyProduct, monthlyProduct, yearlyProduct)

        // When
        viewModel.onTabSelected(TabOption.WEEKlY)

        // Then
        assertEquals(TabOption.WEEKlY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(1, viewModel.filteredProductDetails?.size)
        assertEquals("weekly", viewModel.filteredProductDetails?.first()?.productId)
    }

    @Test
    fun `test onTabSelected with null productDetails`() {
        // Given
        viewModel.productDetails = null

        // When
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertNull(viewModel.filteredProductDetails)
    }

    @Test
    fun `test onTabSelected with empty productDetails`() {
        // Given
        viewModel.productDetails = emptyList()

        // When
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(0, viewModel.filteredProductDetails?.size)
    }

    @Test
    fun `test onTabSelected with no matching products`() {
        // Given
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        viewModel.productDetails = listOf(yearlyProduct)

        // When
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(0, viewModel.filteredProductDetails?.size)
    }

    @Test
    fun `test onTabSelected with multiple matching products`() {
        // Given
        val monthlyProduct1 = createMockProductDetails("monthly1", "P1M")
        val monthlyProduct2 = createMockProductDetails("monthly2", "P1M")
        val yearlyProduct = createMockProductDetails("yearly", "P1Y")
        viewModel.productDetails = listOf(monthlyProduct1, monthlyProduct2, yearlyProduct)

        // When
        viewModel.onTabSelected(TabOption.MONTHLY)

        // Then
        assertEquals(TabOption.MONTHLY, viewModel.selectedTab)
        assertEquals(-1, viewModel.selectedPlan)
        assertEquals(2, viewModel.filteredProductDetails?.size)
    }
} 