package com.synchronoss.aiap.presentation.viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.analytics.AnalyticsUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Test class for the handleIosPlatformProducts method in SubscriptionsViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HandleIosPlatformProductsTest {
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
            ),
            recordPrivateCalls = true
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
     * Helper method to create a mock ProductDetails with specific product id
     */
    private fun createMockProductDetails(productId: String, billingPeriod: String = "P1M"): ProductDetails {
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
    
    /**
     * Helper method to create a mock ProductInfo with specific properties
     */
    private fun createMockProductInfo(
        id: String,
        productId: String,
        serviceLevel: String = "basic",
        recurringPeriodCode: String = "P1M",
        isActive: Boolean = true
    ): ProductInfo {
        return ProductInfo(
            id = id,
            productId = productId,
            displayName = "Test Product $productId",
            description = "Description for $productId",
            vendorName = "Test Vendor",
            appName = "Test App",
            price = 9.99,
            displayPrice = "$9.99",
            platform = "ANDROID",
            serviceLevel = serviceLevel,
            isActive = isActive,
            recurringPeriodCode = recurringPeriodCode,
            productType = "subscription",
            entitlementId = null
        )
    }

    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is false`() {
        // Given
        viewModel.isIosPlatform = false
        viewModel.currentProductId = "android_product"
        val androidProduct = createMockProductDetails("android_product")
        viewModel.productDetails = listOf(androidProduct)
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertEquals(androidProduct, viewModel.currentProductDetails)
    }
    
    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is true with matching product found`() {
        // Given
        viewModel.isIosPlatform = true
        
        // Create iOS active product
        val iosProduct = createMockProductInfo(
            id = "ios_product_id",
            productId = "ios_product",
            serviceLevel = "premium",
            recurringPeriodCode = "P1M"
        )
        viewModel.activeProduct = iosProduct
        
        // Create Android products with one matching the iOS criteria
        val matchingAndroidProduct = createMockProductInfo(
            id = "android_product_id",
            productId = "android_product",
            serviceLevel = "premium", // Same as iOS
            recurringPeriodCode = "P1M" // Same as iOS
        )
        
        val nonMatchingAndroidProduct = createMockProductInfo(
            id = "android_product_id2",
            productId = "android_product2",
            serviceLevel = "basic", // Different from iOS
            recurringPeriodCode = "P1Y" // Different from iOS
        )
        
        viewModel.products = listOf(matchingAndroidProduct, nonMatchingAndroidProduct)
        
        // Create ProductDetails
        val matchingAndroidProductDetails = createMockProductDetails("android_product")
        val nonMatchingAndroidProductDetails = createMockProductDetails("android_product2", "P1Y")
        viewModel.productDetails = listOf(matchingAndroidProductDetails, nonMatchingAndroidProductDetails)
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertEquals(matchingAndroidProduct, viewModel.currentProduct)
        assertEquals("android_product", viewModel.currentProductId)
        assertEquals(matchingAndroidProductDetails, viewModel.currentProductDetails)
    }
    
    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is true with no matching product found`() {
        // Given
        viewModel.isIosPlatform = true
        
        // Create iOS active product
        val iosProduct = createMockProductInfo(
            id = "ios_product_id",
            productId = "ios_product",
            serviceLevel = "premium",
            recurringPeriodCode = "P1M"
        )
        viewModel.activeProduct = iosProduct
        
        // Create Android products with none matching the iOS criteria
        val nonMatchingAndroidProduct1 = createMockProductInfo(
            id = "android_product_id1",
            productId = "android_product1",
            serviceLevel = "basic", // Different from iOS
            recurringPeriodCode = "P1Y" // Different from iOS
        )
        
        val nonMatchingAndroidProduct2 = createMockProductInfo(
            id = "android_product_id2",
            productId = "android_product2",
            serviceLevel = "standard", // Different from iOS
            recurringPeriodCode = "P1W" // Different from iOS
        )
        
        viewModel.products = listOf(nonMatchingAndroidProduct1, nonMatchingAndroidProduct2)
        
        // Create ProductDetails
        val nonMatchingAndroidProductDetails1 = createMockProductDetails("android_product1", "P1Y")
        val nonMatchingAndroidProductDetails2 = createMockProductDetails("android_product2", "P1W")
        viewModel.productDetails = listOf(nonMatchingAndroidProductDetails1, nonMatchingAndroidProductDetails2)
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertNull(viewModel.currentProduct)
        assertNull(viewModel.currentProductId)
        assertNull(viewModel.currentProductDetails)
    }
    
    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is true but activeProduct is null`() {
        // Given
        viewModel.isIosPlatform = true
        viewModel.activeProduct = null
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertNull(viewModel.currentProduct)
        assertNull(viewModel.currentProductId)
        assertNull(viewModel.currentProductDetails)
    }
    
    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is true and matching product found but no matching productDetails`() {
        // Given
        viewModel.isIosPlatform = true
        
        // Create iOS active product
        val iosProduct = createMockProductInfo(
            id = "ios_product_id",
            productId = "ios_product",
            serviceLevel = "premium",
            recurringPeriodCode = "P1M"
        )
        viewModel.activeProduct = iosProduct
        
        // Create Android products with one matching the iOS criteria
        val matchingAndroidProduct = createMockProductInfo(
            id = "android_product_id",
            productId = "android_product",
            serviceLevel = "premium", // Same as iOS
            recurringPeriodCode = "P1M" // Same as iOS
        )
        
        viewModel.products = listOf(matchingAndroidProduct)
        
        // Create ProductDetails with different product ID (no match)
        val nonMatchingProductDetails = createMockProductDetails("different_product_id")
        viewModel.productDetails = listOf(nonMatchingProductDetails)
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertEquals(matchingAndroidProduct, viewModel.currentProduct)
        assertEquals("android_product", viewModel.currentProductId)
        assertNull(viewModel.currentProductDetails) // No matching product details
    }
    
    @Test
    fun `test handleIosPlatformProducts when isIosPlatform is false but currentProductId does not match any product`() {
        // Given
        viewModel.isIosPlatform = false
        viewModel.currentProductId = "non_existent_product"
        val androidProduct = createMockProductDetails("android_product")
        viewModel.productDetails = listOf(androidProduct)
        
        // When
        viewModel.handleIosPlatformProducts()
        
        // Then
        assertNull(viewModel.currentProductDetails)
    }
} 