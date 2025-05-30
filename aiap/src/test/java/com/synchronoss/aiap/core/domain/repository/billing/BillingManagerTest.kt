package com.synchronoss.aiap.core.domain.repository.billing

import android.content.Context
import androidx.activity.ComponentActivity
import com.android.billingclient.api.*
import com.synchronoss.aiap.core.data.repository.billing.BillingManagerImpl
import com.synchronoss.aiap.core.data.repository.billing.GlobalBillingConfig
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.usecases.product.*
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.fail
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.synchronoss.aiap.core.data.repository.billing.BillingManagerConstants

class BillingManagerTest {
    private lateinit var billingManager: BillingManager
    private lateinit var mockContext: Context
    private lateinit var mockProductManagerUseCases: ProductManagerUseCases
    private lateinit var mockPurchaseUpdateHandler: PurchaseUpdateHandler
    private lateinit var mockActivity: ComponentActivity
    private lateinit var mockBillingClient: BillingClient
    private val testApiKey = "test_api_key"
    private val testUserId = "test_user_id"
    private val testUserUUID = "test_user_uuid"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockContext = mockk(relaxed = true)
        mockPurchaseUpdateHandler = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockBillingClient = mockk(relaxed = true)

        // Initialize GlobalBillingConfig with test values
        GlobalBillingConfig.userUUID = testUserUUID
        GlobalBillingConfig.partnerUserId = testUserId
        GlobalBillingConfig.apiKey = testApiKey

        // Mock ProductManagerUseCases
        val mockGetProductsApi = mockk<GetProductsApi>(relaxed = true)
        val mockGetActiveSubscription = mockk<GetActiveSubscription>(relaxed = true)
        val mockHandlePurchase = mockk<HandlePurchase>(relaxed = true)
        mockProductManagerUseCases = ProductManagerUseCases(
            getProductsApi = mockGetProductsApi,
            getActiveSubscription = mockGetActiveSubscription,
            handlePurchase = mockHandlePurchase
        )

        mockkStatic(BillingClient::class)
        mockkStatic(PendingPurchasesParams::class)
        mockkStatic(BillingFlowParams::class)

        val mockPendingPurchasesParams = mockk<PendingPurchasesParams>(relaxed = true)
        every {
            PendingPurchasesParams.newBuilder()
        } returns mockk(relaxed = true) {
            every { enableOneTimeProducts() } returns this
            every { build() } returns mockPendingPurchasesParams
        }

        val mockBillingClientBuilder = mockk<BillingClient.Builder>(relaxed = true) {
            every { setListener(any()) } returns this
            every { enablePendingPurchases(any()) } returns this
            every { build() } returns mockBillingClient
        }

        every {
            BillingClient.newBuilder(any())
        } returns mockBillingClientBuilder

        every {
            mockBillingClient.connectionState
        } returns BillingClient.ConnectionState.CONNECTED

        billingManager = BillingManagerImpl(
            context = mockContext,
            productManagerUseCases = mockProductManagerUseCases,
            purchaseUpdateHandler = mockPurchaseUpdateHandler
        )
    }

    @Test
    fun `test startConnection success`() = runTest {
        val deferred = CompletableDeferred<Unit>()
        
        coEvery { 
            mockBillingClient.startConnection(any()) 
        } answers {
            firstArg<BillingClientStateListener>().onBillingSetupFinished(
                mockk {
                    every { responseCode } returns BillingClient.BillingResponseCode.OK
                }
            )
            deferred.complete(Unit)
        }

        val result = billingManager.startConnection()
        assertTrue(result.isCompleted)
    }

    @Test
    fun `test checkExistingSubscriptions success`() = runTest {
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }

        val mockPurchase = mockk<Purchase> {
            every { products } returns listOf("test_product")
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
        }

        coEvery { 
            mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) 
        } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                listOf(mockPurchase)
            )
        }

        val mockProductDetails = mockk<ProductDetails>(relaxed = true)
        coEvery {
            mockBillingClient.queryProductDetailsAsync(any(), any())
        } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(
                mockBillingResult,
                listOf(mockProductDetails)
            )
        }

        var errorCalled = false
        val result = billingManager.checkExistingSubscriptions { errorCalled = true }
        assertTrue(result == mockProductDetails)
        assertTrue(!errorCalled)
    }

    @Test
    fun `test purchaseSubscription success`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        val billingResult = mockk<BillingResult>()
        val subscriptionOfferDetails = listOf(
            mockk<ProductDetails.SubscriptionOfferDetails> {
                every { offerToken } returns "test_offer_token"
            }
        )

        // Mock BillingFlowParams and Builder
        val mockProductDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        val mockProductDetailsParamsBuilder = mockk<BillingFlowParams.ProductDetailsParams.Builder> {
            every { setProductDetails(any()) } returns this
            every { setOfferToken(any()) } returns this
            every { build() } returns mockProductDetailsParams
        }

        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }

        mockkStatic(BillingFlowParams.ProductDetailsParams::class)
        every { 
            BillingFlowParams.ProductDetailsParams.newBuilder() 
        } returns mockProductDetailsParamsBuilder

        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        every { mockBillingClient.isReady } returns true
        every { mockBillingClient.launchBillingFlow(any(), any()) } returns billingResult
        every { billingResult.responseCode } returns BillingClient.BillingResponseCode.OK
        every { productDetails.subscriptionOfferDetails } returns subscriptionOfferDetails
        every { productDetails.productId } returns "test_product_id"

        // When
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { fail("Error callback should not be called") },
            userId = testUserId,
            apiKey = testApiKey
        )

        // Then
        verify { mockBillingClient.launchBillingFlow(eq(activity), any()) }
    }

    @Test
    fun `test handleUnacknowledgedPurchases success`() = runTest {
        // Given
        val productId = "test_product_id"
        val purchaseTime = 1234567890L
        val purchaseToken = "test_purchase_token"
        val purchase = mockk<Purchase>()
        val purchaseList = listOf(purchase)
        val billingResult = mockk<BillingResult>()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                billingResult, 
                purchaseList
            )
        }
        
        every { billingResult.responseCode } returns BillingClient.BillingResponseCode.OK
        every { purchase.isAcknowledged } returns false
        every { purchase.purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchase.products } returns listOf(productId)
        every { purchase.purchaseTime } returns purchaseTime
        every { purchase.purchaseToken } returns purchaseToken

        coEvery { 
            mockProductManagerUseCases.handlePurchase(
                productId = productId,
                purchaseTime = purchaseTime,
                purchaseToken = purchaseToken,
                partnerUserId = any(),
                apiKey = any()
            ) 
        } returns true

        // When
        val result = billingManager.handleUnacknowledgedPurchases { }

        // Then
        assertTrue(result)
        coVerify { 
            mockProductManagerUseCases.handlePurchase(
                productId = productId,
                purchaseTime = purchaseTime,
                purchaseToken = purchaseToken,
                partnerUserId = any(),
                apiKey = any()
            )
        }
    }

    @Test
    fun `test getProductDetails success`() = runTest {
        // Given
        val productIds = listOf("test_product_id")
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        val mockProductDetails = mockk<ProductDetails>(relaxed = true)
        
        // QueryProductDetailsParams setup
        val mockProduct = mockk<QueryProductDetailsParams.Product>()
        val mockProductBuilder = mockk<QueryProductDetailsParams.Product.Builder> {
            every { setProductId(any()) } returns this
            every { setProductType(any()) } returns this
            every { build() } returns mockProduct
        }
        
        val mockParams = mockk<QueryProductDetailsParams>()
        val mockParamsBuilder = mockk<QueryProductDetailsParams.Builder> {
            every { setProductList(any()) } returns this
            every { build() } returns mockParams
        }
        
        mockkStatic(QueryProductDetailsParams.Product::class)
        mockkStatic(QueryProductDetailsParams::class)
        
        every { QueryProductDetailsParams.Product.newBuilder() } returns mockProductBuilder
        every { QueryProductDetailsParams.newBuilder() } returns mockParamsBuilder
        
        // Mock the API call
        every {
            mockBillingClient.queryProductDetailsAsync(any(), any())
        } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(
                mockBillingResult,
                listOf(mockProductDetails)
            )
        }
        
        // When
        var errorCalled = false
        val result = billingManager.getProductDetails(productIds) { errorCalled = true }
        
        // Then
        assertTrue(result?.isNotEmpty() == true)
        assertTrue(!errorCalled)
        verify { mockBillingClient.queryProductDetailsAsync(any(), any()) }
    }
    
    @Test
    fun `test getProductDetails with client not connected`() = runTest {
        // Given
        val productIds = listOf("test_product_id")
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.DISCONNECTED
        
        // When
        var errorMessage = ""
        val result = billingManager.getProductDetails(productIds) { errorMessage = it }
        
        // Then
        assertNull(result)
        assertTrue(errorMessage == BillingManagerConstants.ERROR_BILLING_CLIENT_NOT_CONNECTED)
    }

    @Test
    fun `test updateSubscription success`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        val subscriptionOfferDetails = listOf(
            mockk<ProductDetails.SubscriptionOfferDetails> {
                every { offerToken } returns "test_offer_token"
            }
        )
        val mockCurrentPurchase = mockk<Purchase> {
            every { purchaseToken } returns "existing_purchase_token"
        }
        
        // Set current purchase in BillingManagerImpl via reflection
        val billingManagerImpl = billingManager as BillingManagerImpl
        val field = BillingManagerImpl::class.java.getDeclaredField("currentProduct")
        field.isAccessible = true
        field.set(billingManagerImpl, mockCurrentPurchase)
        
        // Mock BillingFlowParams and Builder
        val mockProductDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        val mockProductDetailsParamsBuilder = mockk<BillingFlowParams.ProductDetailsParams.Builder> {
            every { setProductDetails(any()) } returns this
            every { setOfferToken(any()) } returns this
            every { build() } returns mockProductDetailsParams
        }
        
        val mockSubscriptionUpdateParams = mockk<BillingFlowParams.SubscriptionUpdateParams>()
        val mockSubscriptionUpdateParamsBuilder = mockk<BillingFlowParams.SubscriptionUpdateParams.Builder> {
            every { setOldPurchaseToken(any()) } returns this
            every { setSubscriptionReplacementMode(any()) } returns this
            every { build() } returns mockSubscriptionUpdateParams
        }
        
        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { setSubscriptionUpdateParams(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }
        
        val billingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        mockkStatic(BillingFlowParams.ProductDetailsParams::class)
        mockkStatic(BillingFlowParams.SubscriptionUpdateParams::class)
        every { BillingFlowParams.ProductDetailsParams.newBuilder() } returns mockProductDetailsParamsBuilder
        every { BillingFlowParams.SubscriptionUpdateParams.newBuilder() } returns mockSubscriptionUpdateParamsBuilder
        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        
        every { productDetails.subscriptionOfferDetails } returns subscriptionOfferDetails
        every { productDetails.productId } returns "test_product_id"
        every { mockBillingClient.launchBillingFlow(any(), any()) } returns billingResult
        
        // When
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { fail("Error callback should not be called") },
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Then
        verify { mockBillingClient.launchBillingFlow(eq(activity), any()) }
        verify { mockSubscriptionUpdateParamsBuilder.setOldPurchaseToken("existing_purchase_token") }
    }

    @Test
    fun `test onPurchasesUpdated successful purchase`() = runTest {
        // Given
        val testProductId = "test_product_id"
        val testPurchaseTime = 1234567890L
        val testPurchaseToken = "test_purchase_token"
        
        val billingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        val mockPurchase = mockk<Purchase> {
            every { products } returns listOf(testProductId)
            every { purchaseTime } returns testPurchaseTime
            every { purchaseToken } returns testPurchaseToken
        }
        
        coEvery { 
            mockProductManagerUseCases.handlePurchase(
                productId = testProductId,
                purchaseTime = testPurchaseTime,
                purchaseToken = testPurchaseToken,
                partnerUserId = any(),
                apiKey = any()
            ) 
        } returns true
        
        // When - Call the method directly on the implementation
        (billingManager as BillingManagerImpl).onPurchasesUpdated(
            billingResult, 
            listOf(mockPurchase)
        )
        
        // Then
        verify { mockPurchaseUpdateHandler.handlePurchaseStarted() }
        coVerify { 
            mockProductManagerUseCases.handlePurchase(
                productId = testProductId,
                purchaseTime = testPurchaseTime,
                purchaseToken = testPurchaseToken,
                partnerUserId = any(),
                apiKey = any()
            )
        }
        verify { mockPurchaseUpdateHandler.handlePurchaseUpdate() }
    }
    
    @Test
    fun `test onPurchasesUpdated failed purchase`() = runTest {
        // Given
        val billingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
        }
        
        // When
        (billingManager as BillingManagerImpl).onPurchasesUpdated(
            billingResult, 
            emptyList()
        )
        
        // Then
        verify { mockPurchaseUpdateHandler.handlePurchaseStarted() }
        verify { mockPurchaseUpdateHandler.handlePurchaseStopped() }
        verify(exactly = 0) { mockPurchaseUpdateHandler.handlePurchaseUpdate() }
    }
    
    @Test
    fun `test onPurchasesUpdated successful but API fails`() = runTest {
        // Given
        val testProductId = "test_product_id"
        val testPurchaseTime = 1234567890L
        val testPurchaseToken = "test_purchase_token"
        
        val billingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        val mockPurchase = mockk<Purchase> {
            every { products } returns listOf(testProductId)
            every { purchaseTime } returns testPurchaseTime
            every { purchaseToken } returns testPurchaseToken
        }
        
        // Set up coEvery to run the callback immediately
        coEvery { 
            mockProductManagerUseCases.handlePurchase(
                productId = testProductId,
                purchaseTime = testPurchaseTime,
                purchaseToken = testPurchaseToken,
                partnerUserId = any(),
                apiKey = any()
            ) 
        } coAnswers {
            // Simulate API call completing
            false
        }

        // When
        (billingManager as BillingManagerImpl).onPurchasesUpdated(
            billingResult, 
            listOf(mockPurchase)
        )
        
        // Then - Since we're using coAnswers to simulate immediate completion,
        // we can verify without waiting
        verifyOrder {
            mockPurchaseUpdateHandler.handlePurchaseStarted()
            mockPurchaseUpdateHandler.handlePurchaseFailed()
        }
        verify(exactly = 0) { mockPurchaseUpdateHandler.handlePurchaseUpdate() }
    }

    @Test
    fun `test purchaseSubscription with error in billing flow`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        val billingResult = mockk<BillingResult>()
        val subscriptionOfferDetails = listOf(
            mockk<ProductDetails.SubscriptionOfferDetails> {
                every { offerToken } returns "test_offer_token"
            }
        )

        // Mock BillingFlowParams and Builder
        val mockProductDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        val mockProductDetailsParamsBuilder = mockk<BillingFlowParams.ProductDetailsParams.Builder> {
            every { setProductDetails(any()) } returns this
            every { setOfferToken(any()) } returns this
            every { build() } returns mockProductDetailsParams
        }

        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }

        mockkStatic(BillingFlowParams.ProductDetailsParams::class)
        every { BillingFlowParams.ProductDetailsParams.newBuilder() } returns mockProductDetailsParamsBuilder
        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        
        every { mockBillingClient.isReady } returns true
        every { mockBillingClient.launchBillingFlow(any(), any()) } returns billingResult
        every { billingResult.responseCode } returns BillingClient.BillingResponseCode.ERROR
        every { billingResult.debugMessage } returns "Mock billing error"
        every { productDetails.subscriptionOfferDetails } returns subscriptionOfferDetails
        every { productDetails.productId } returns "test_product_id"

        // When
        var errorMessage = ""
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { errorMessage = it },
            userId = testUserId,
            apiKey = testApiKey
        )

        // Then
        verify { mockBillingClient.launchBillingFlow(eq(activity), any()) }
        assertTrue(errorMessage == "Mock billing error")
    }
    
    @Test
    fun `test purchaseSubscription with exception`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails> {
            every { productId } returns "test_product_id"
            every { subscriptionOfferDetails } throws RuntimeException("Test exception")
        }
        val errorMessage = "Test exception"
        
        // When
        var capturedError = ""
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { capturedError = it },
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Then
        assertEquals(errorMessage, capturedError)
    }

    @Test
    fun `test onBillingServiceDisconnected`() = runTest {
        // Given
        var listenerCaptured: BillingClientStateListener? = null
        
        coEvery { 
            mockBillingClient.startConnection(capture(slot<BillingClientStateListener>()))
        } answers {
            listenerCaptured = firstArg()
            Unit
        }
        
        // When
        val result = billingManager.startConnection()
        
        // Manually trigger the disconnection
        listenerCaptured?.onBillingServiceDisconnected()
        
        // Then - Just check that it doesn't throw an exception
        // Try to complete to avoid hanging test
        try {
            result.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    @Test
    fun `test startConnection with failure`() = runTest {
        val deferred = CompletableDeferred<Unit>()
        val errorMessage = "Test billing setup error"
        
        coEvery { 
            mockBillingClient.startConnection(any()) 
        } answers {
            firstArg<BillingClientStateListener>().onBillingSetupFinished(
                mockk {
                    every { responseCode } returns BillingClient.BillingResponseCode.ERROR
                    every { debugMessage } returns errorMessage
                }
            )
            deferred
        }

        val result = billingManager.startConnection()
        
        assertTrue(result.isCompleted)
        // Check for exceptional completion differently
        var completedExceptionally = false
        try {
            result.await()
        } catch (e: Exception) {
            completedExceptionally = true
            assertTrue(e.message?.contains(errorMessage) == true)
        }
        assertTrue(completedExceptionally)
    }

    // Tests merged from BillingManagerImplConditionsTest
    
    @Test
    fun `test purchaseSubscription handles exceptions`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        val mockException = RuntimeException("Test exception")
        
        // Setup the exception to be thrown
        every { 
            productDetails.subscriptionOfferDetails
        } throws mockException
        
        // Track if error callback is called
        var errorCallbackInvoked = false
        val onError: (String) -> Unit = { errorCallbackInvoked = true }
        
        // When
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = onError,
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Then - Just verify the error callback was invoked
        assertTrue(errorCallbackInvoked, "Error callback should be invoked when exception is thrown")
    }
    
    @Test
    fun `test handleProductDetails with null productDetails`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val emptyProductDetailsList = emptyList<ProductDetails>()
        var errorCalled = false
        val onError: (String) -> Unit = { errorCalled = true }
        
        // When - Using reflection to access private method
        val method = BillingManagerImpl::class.java.getDeclaredMethod(
            "handleProductDetails",
            ComponentActivity::class.java,
            List::class.java,
            Function1::class.java
        )
        method.isAccessible = true
        method.invoke(billingManager, activity, emptyProductDetailsList, onError)
        
        // Then
        assertTrue(errorCalled)
    }
    
    @Test
    fun `test launchBillingFlow with error response code`() = runTest {
        // Given
        val activity = mockk<ComponentActivity>()
        val productDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        var errorCalled = false
        val onError: (String) -> Unit = { errorCalled = true }
        
        // Mock BillingFlowParams
        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }
        
        mockkStatic(BillingFlowParams::class)
        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        
        // Mock billing response with error
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "Test error message"
        }
        
        every { 
            mockBillingClient.launchBillingFlow(any(), any()) 
        } returns mockBillingResult
        
        // When - Using reflection to access private method
        val method = BillingManagerImpl::class.java.getDeclaredMethod(
            "launchBillingFlow",
            ComponentActivity::class.java,
            List::class.java,
            Function1::class.java
        )
        method.isAccessible = true
        method.invoke(billingManager, activity, listOf(productDetailsParams), onError)
        
        // Then
        assertTrue(errorCalled)
    }
    
    @Test
    fun `test createUpdateFlowParams with null currentProduct`() = runTest {
        // Given
        val productDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        
        // Clear any existing currentProduct
        val billingManagerImpl = billingManager as BillingManagerImpl
        val field = BillingManagerImpl::class.java.getDeclaredField("currentProduct")
        field.isAccessible = true
        field.set(billingManagerImpl, null)
        
        // When - Using reflection to access private method
        val method = BillingManagerImpl::class.java.getDeclaredMethod(
            "createUpdateFlowParams",
            List::class.java
        )
        method.isAccessible = true
        val result = method.invoke(billingManager, listOf(productDetailsParams))
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `test createUpdateFlowParams with non-null currentProduct`() = runTest {
        // Given
        val productDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        val mockCurrentPurchase = mockk<Purchase> {
            every { purchaseToken } returns "test_purchase_token"
        }
        
        // Set current purchase using reflection
        val billingManagerImpl = billingManager as BillingManagerImpl
        val field = BillingManagerImpl::class.java.getDeclaredField("currentProduct")
        field.isAccessible = true
        field.set(billingManagerImpl, mockCurrentPurchase)
        
        // Mock SubscriptionUpdateParams
        val mockSubscriptionUpdateParams = mockk<BillingFlowParams.SubscriptionUpdateParams>()
        val mockSubscriptionUpdateParamsBuilder = mockk<BillingFlowParams.SubscriptionUpdateParams.Builder> {
            every { setOldPurchaseToken(any()) } returns this
            every { setSubscriptionReplacementMode(any()) } returns this
            every { build() } returns mockSubscriptionUpdateParams
        }
        
        // Mock BillingFlowParams
        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { setSubscriptionUpdateParams(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }
        
        mockkStatic(BillingFlowParams::class)
        mockkStatic(BillingFlowParams.SubscriptionUpdateParams::class)
        every { BillingFlowParams.SubscriptionUpdateParams.newBuilder() } returns mockSubscriptionUpdateParamsBuilder
        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        
        // When - Using reflection to access private method
        val method = BillingManagerImpl::class.java.getDeclaredMethod(
            "createUpdateFlowParams",
            List::class.java
        )
        method.isAccessible = true
        val result = method.invoke(billingManager, listOf(productDetailsParams))
        
        // Then
        assertEquals(mockBillingFlowParams, result)
        verify { mockSubscriptionUpdateParamsBuilder.setOldPurchaseToken("test_purchase_token") }
    }
    
    @Test
    fun `test launchBillingFlow with currentProduct for update path`() = runTest {
        // Setup for testing line 178 branch
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        val mockCurrentPurchase = mockk<Purchase>(relaxed = true)
        
        // Set current purchase to test update flow
        val billingManagerImpl = billingManager as BillingManagerImpl
        val field = BillingManagerImpl::class.java.getDeclaredField("currentProduct")
        field.isAccessible = true
        field.set(billingManagerImpl, mockCurrentPurchase)
        
        // Setup necessary mocks
        val subscriptionOfferDetails = listOf(
            mockk<ProductDetails.SubscriptionOfferDetails> {
                every { offerToken } returns "test_offer_token"
            }
        )
        every { productDetails.subscriptionOfferDetails } returns subscriptionOfferDetails
        every { productDetails.productId } returns "test_product_id"
        
        // All the necessary mocks for billing flow
        mockBillingFlowProcess()
        
        // Execute
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { fail("Error callback should not be called") },
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Verify update flow was used
        verify(exactly = 1) { mockBillingClient.launchBillingFlow(eq(activity), any()) }
    }

    @Test
    fun `test checkExistingSubscriptions when client disconnected`() = runTest {
        // Setup to test lines 209-211
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.DISCONNECTED
        
        var errorMessage = ""
        val result = billingManager.checkExistingSubscriptions { errorMessage = it }
        
        // Verify error and null result
        assertTrue(errorMessage == BillingManagerConstants.ERROR_BILLING_CLIENT_NOT_CONNECTED)
        assertNull(result)
    }

    @Test
    fun `test checkExistingSubscriptions with billing error`() = runTest {
        // Setup to test lines 222-224
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "Billing client error"
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                emptyList()
            )
        }
        
        var errorMessage = ""
        val result = billingManager.checkExistingSubscriptions { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage.contains("Failed to query purchases"))
        assertNull(result)
    }

    @Test
    fun `test checkExistingSubscriptions with no subscription found`() = runTest {
        // Setup to test line 227
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                emptyList() // No subscriptions found
            )
        }
        
        var errorCalled = false
        val result = billingManager.checkExistingSubscriptions { errorCalled = true }
        
        // Verify no error and null result
        assertFalse(errorCalled)
        assertNull(result)
    }

    @Test
    fun `test checkExistingSubscriptions with subscription but no product ID`() = runTest {
        // Setup to test line 231
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        val mockPurchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { products } returns emptyList() // No product ID
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                listOf(mockPurchase)
            )
        }
        
        var errorCalled = false
        val result = billingManager.checkExistingSubscriptions { errorCalled = true }
        
        // Verify no error and null result
        assertFalse(errorCalled)
        assertNull(result)
    }

    @Test
    fun `test checkExistingSubscriptions with query product details failure`() = runTest {
        // Setup to test line 244+
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        val mockErrorResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "Failed to get product details"
        }
        
        val mockPurchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { products } returns listOf("test_product_id")
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                listOf(mockPurchase)
            )
        }
        
        // Mock product details failure
        every { mockBillingClient.queryProductDetailsAsync(any(), any()) } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(
                mockErrorResult,
                emptyList()
            )
        }
        
        var errorMessage = ""
        val result = billingManager.checkExistingSubscriptions { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage == "Failed to get product details")
        assertNull(result)
    }

    @Test
    fun `test checkExistingSubscriptions with exception`() = runTest {
        // Setup to test lines 247-252, 255, 259-260
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } throws RuntimeException("Test exception")
        
        var errorMessage = ""
        val result = billingManager.checkExistingSubscriptions { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage.contains("Error checking subscriptions"))
        assertNull(result)
    }

    @Test
    fun `test getProductDetails with disconnected client`() = runTest {
        // Setup to test line 264, 266
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.DISCONNECTED
        
        var errorMessage = ""
        val result = billingManager.getProductDetails(listOf("test_product")) { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage == BillingManagerConstants.ERROR_BILLING_CLIENT_NOT_CONNECTED)
        assertNull(result)
    }

    @Test
    fun `test getProductDetails with billing error`() = runTest {
        // Setup for line 290
        val productIds = listOf("test_product_id")
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "Query product details failed"
        }
        
        // Mock product details API call
        setupProductDetailsQueryMocks()
        
        every { mockBillingClient.queryProductDetailsAsync(any(), any()) } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(
                mockBillingResult,
                emptyList()
            )
        }
        
        var errorMessage = ""
        val result = billingManager.getProductDetails(productIds) { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage == "Query product details failed")
        assertNull(result)
    }

    @Test
    fun `test getProductDetails with exception`() = runTest {
        // Setup for lines 342, 345, 350, 352
        val productIds = listOf("test_product_id")
        setupProductDetailsQueryMocks()
        
        every { mockBillingClient.queryProductDetailsAsync(any(), any()) } throws RuntimeException("Test exception")
        
        var errorMessage = ""
        val result = billingManager.getProductDetails(productIds) { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage.contains("Test exception"))
        assertNull(result)
    }

    @Test
    fun `test handleUnacknowledgedPurchases with client disconnected`() = runTest {
        // Setup for lines 358-360
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.DISCONNECTED
        
        var errorMessage = ""
        val result = billingManager.handleUnacknowledgedPurchases { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage == BillingManagerConstants.ERROR_BILLING_CLIENT_NOT_CONNECTED)
        assertFalse(result)
    }

    @Test
    fun `test handleUnacknowledgedPurchases with billing error`() = runTest {
        // Setup for lines 372-377
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "Query purchases failed"
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                emptyList()
            )
        }
        
        var errorMessage = ""
        val result = billingManager.handleUnacknowledgedPurchases { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage.contains("Failed to query purchases"))
        assertFalse(result)
    }

    @Test
    fun `test handleUnacknowledgedPurchases with unacknowledged purchase and API failure`() = runTest {
        // Setup for line 379, 388
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        val mockPurchase = mockk<Purchase> {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { products } returns listOf("test_product_id")
            every { purchaseTime } returns 1234567890L
            every { purchaseToken } returns "test_purchase_token"
        }
        
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(
                mockBillingResult,
                listOf(mockPurchase)
            )
        }
        
        // Mock API failure
        coEvery { 
            mockProductManagerUseCases.handlePurchase(
                productId = any(),
                purchaseTime = any(),
                purchaseToken = any(),
                partnerUserId = any(),
                apiKey = any()
            ) 
        } returns false
        
        var errorCalled = false
        val result = billingManager.handleUnacknowledgedPurchases { errorCalled = true }
        
        // Verify handling
        assertFalse(result)
        assertFalse(errorCalled)
    }

    @Test
    fun `test handleUnacknowledgedPurchases with exception`() = runTest {
        // Setup for lines 397-398, 407, 411
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.CONNECTED
        every { mockBillingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } throws RuntimeException("Test exception")
        
        var errorMessage = ""
        val result = billingManager.handleUnacknowledgedPurchases { errorMessage = it }
        
        // Verify error handling
        assertTrue(errorMessage.contains("Test exception"))
        assertFalse(result)
    }

    // Helper method to setup product details query mocks
    private fun setupProductDetailsQueryMocks() {
        val mockProduct = mockk<QueryProductDetailsParams.Product>()
        val mockProductBuilder = mockk<QueryProductDetailsParams.Product.Builder> {
            every { setProductId(any()) } returns this
            every { setProductType(any()) } returns this
            every { build() } returns mockProduct
        }
        
        val mockParams = mockk<QueryProductDetailsParams>()
        val mockParamsBuilder = mockk<QueryProductDetailsParams.Builder> {
            every { setProductList(any()) } returns this
            every { build() } returns mockParams
        }
        
        mockkStatic(QueryProductDetailsParams.Product::class)
        mockkStatic(QueryProductDetailsParams::class)
        
        every { QueryProductDetailsParams.Product.newBuilder() } returns mockProductBuilder
        every { QueryProductDetailsParams.newBuilder() } returns mockParamsBuilder
    }

    // Helper method to setup billing flow process mocks
    private fun mockBillingFlowProcess() {
        val mockProductDetailsParams = mockk<BillingFlowParams.ProductDetailsParams>()
        val mockProductDetailsParamsBuilder = mockk<BillingFlowParams.ProductDetailsParams.Builder> {
            every { setProductDetails(any()) } returns this
            every { setOfferToken(any()) } returns this
            every { build() } returns mockProductDetailsParams
        }
        
        val mockSubscriptionUpdateParams = mockk<BillingFlowParams.SubscriptionUpdateParams>()
        val mockSubscriptionUpdateParamsBuilder = mockk<BillingFlowParams.SubscriptionUpdateParams.Builder> {
            every { setOldPurchaseToken(any()) } returns this
            every { setSubscriptionReplacementMode(any()) } returns this
            every { build() } returns mockSubscriptionUpdateParams
        }
        
        val mockBillingFlowParams = mockk<BillingFlowParams>()
        val mockBillingFlowParamsBuilder = mockk<BillingFlowParams.Builder> {
            every { setProductDetailsParamsList(any()) } returns this
            every { setObfuscatedAccountId(any()) } returns this
            every { setObfuscatedProfileId(any()) } returns this
            every { setSubscriptionUpdateParams(any()) } returns this
            every { build() } returns mockBillingFlowParams
        }
        
        val billingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        mockkStatic(BillingFlowParams.ProductDetailsParams::class)
        mockkStatic(BillingFlowParams.SubscriptionUpdateParams::class)
        every { BillingFlowParams.ProductDetailsParams.newBuilder() } returns mockProductDetailsParamsBuilder
        every { BillingFlowParams.SubscriptionUpdateParams.newBuilder() } returns mockSubscriptionUpdateParamsBuilder
        every { BillingFlowParams.newBuilder() } returns mockBillingFlowParamsBuilder
        
        every { mockBillingClient.launchBillingFlow(any(), any()) } returns billingResult
    }

    @Test
    fun `test purchaseSubscription with exception handling`() = runTest {
        // Setup to trigger the catch block in line 95
        val activity = mockk<ComponentActivity>()
        val productDetails = mockk<ProductDetails>()
        
        every { productDetails.subscriptionOfferDetails } throws RuntimeException("Test exception")
        
        var errorMessage = ""
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = productDetails,
            onError = { errorMessage = it },
            userId = testUserId,
            apiKey = testApiKey
        )
        
        // Just verify the error message is not empty - the exact message might vary
        assertTrue(errorMessage.isNotEmpty())
    }
}