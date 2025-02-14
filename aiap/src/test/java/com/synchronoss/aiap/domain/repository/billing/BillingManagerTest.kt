package com.synchronoss.aiap.domain.repository.billing

import android.content.Context
import androidx.activity.ComponentActivity
import com.android.billingclient.api.*
import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseResponse
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.repository.billing.BillingManagerImpl
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.CheckExistingSubscription
import com.synchronoss.aiap.core.domain.usecases.billing.PurchaseSubscription
import com.synchronoss.aiap.core.domain.usecases.billing.StartConnection
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingManagerTest {
    private lateinit var billingManager: BillingManagerImpl
    private lateinit var billingManagerUseCases: BillingManagerUseCases
    private lateinit var mockContext: Context
    private lateinit var mockProductApi: ProductApi
    private lateinit var mockPurchaseUpdateHandler: PurchaseUpdateHandler
    private lateinit var mockActivity: ComponentActivity
    private lateinit var mockBillingClient: BillingClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockContext = mockk(relaxed = true)
        mockProductApi = mockk(relaxed = true)
        mockPurchaseUpdateHandler = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockBillingClient = mockk(relaxed = true)

        mockkStatic(BillingClient::class)
        every {
            BillingClient.newBuilder(any())
        } returns mockk(relaxed = true) {
            every { setListener(any()) } returns this
            every { enablePendingPurchases() } returns this
            every { build() } returns mockBillingClient
        }

        billingManager = BillingManagerImpl(
            mockContext,
            mockProductApi,
            mockPurchaseUpdateHandler
        )

        billingManagerUseCases = BillingManagerUseCases(
            startConnection = StartConnection(billingManager),
            purchaseSubscription = PurchaseSubscription(billingManager),
            checkExistingSubscription = CheckExistingSubscription(billingManager)
        )
    }

    @Test
    fun `startConnection calls onConnected when billing setup is successful`() = runTest {
        // Given
        var connectedCalled = false
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }

        coEvery {
            mockBillingClient.startConnection(any())
        } answers {
            firstArg<BillingClientStateListener>().onBillingSetupFinished(mockBillingResult)
            true
        }

        // When
        billingManager.startConnection(
            onConnected = {
                connectedCalled = true
                assertTrue(connectedCalled)
            },
            onDisconnected = { }
        )

        // Then

    }

    @Test
    fun `startConnection calls onDisconnected when service disconnects`() = runTest {
        // Given
        var disconnectedCalled = false
        // When
            billingManager.startConnection(
                onConnected = {
                        coEvery {
                            mockBillingClient.startConnection(any())
                        } answers {
                            firstArg<BillingClientStateListener>().onBillingServiceDisconnected()
//                            true
                        }
                },
                onDisconnected = {
                    disconnectedCalled = true
                    assertTrue(disconnectedCalled)
                }
            )
        // Then
    }

    @Test
    fun `purchaseSubscription handles error when client is not connected`() = runTest {
        // Given
        var errorMessage: String? = "Billing client not connected"
        val mockProductInfo = mockk<ProductInfo> {
            every { productId } returns "test-product-id"
        }
        every { mockBillingClient.connectionState } returns BillingClient.ConnectionState.DISCONNECTED

        // When
        billingManager.purchaseSubscription(
            activity = mockActivity,
            productDetails = mockProductInfo,
            onError = { errorMessage = it },
            userId = "test-user"
        )

        // Then
        assertEquals("Billing client not connected", errorMessage)
    }


    @Test
    fun `onPurchasesUpdated handles successful purchase`() = runTest {
        // Given
        val mockPurchase = mockk<Purchase> {
            every { purchaseToken } returns "test-token"
            every { products } returns listOf("test-product")
            every { purchaseTime } returns 123456789L
        }
        val mockBillingResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        val mockResponse = mockk<Response<ApiResponse<HandlePurchaseResponse>>> {
            every { isSuccessful } returns true
            every { body() } returns mockk {
                every { code } returns 200
            }
        }

        coEvery {
            mockProductApi.handlePurchase(any())
        } returns mockResponse

        every { mockPurchaseUpdateHandler.handlePurchaseStarted() } just Runs
        every { mockPurchaseUpdateHandler.handlePurchaseUpdate() } just Runs

        // When
        billingManager.onPurchasesUpdated(mockBillingResult, mutableListOf(mockPurchase))

        // Then
        verify { mockPurchaseUpdateHandler.handlePurchaseStarted() }
        verify { mockPurchaseUpdateHandler.handlePurchaseUpdate() }
        coVerify {
            mockProductApi.handlePurchase(match {
                it.purchaseToken == "test-token" &&
                        it.productId == "test-product" &&
                        it.purchaseTime == 123456789L
            })
        }
    }
}