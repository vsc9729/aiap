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
}