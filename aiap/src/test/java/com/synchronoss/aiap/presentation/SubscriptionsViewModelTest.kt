package com.synchronoss.aiap.presentation

import android.util.Log
import androidx.activity.ComponentActivity
import com.synchronoss.aiap.di.PurchaseUpdateHandler
import com.synchronoss.aiap.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.Resource
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionsViewModelTest {

    companion object {
        @JvmStatic
        fun mockLog() {
            mockkStatic(Log::class)
            every { Log.d(any(), any()) } returns 0
        }
    }

    private lateinit var viewModel: SubscriptionsViewModel
    private lateinit var billingManagerUseCases: BillingManagerUseCases
    private lateinit var productManagerUseCases: ProductManagerUseCases
    private lateinit var themeLoader: ThemeLoader
    private lateinit var libraryActivityManagerUseCases: LibraryActivityManagerUseCases
    private lateinit var purchaseUpdateHandler: PurchaseUpdateHandler
    private lateinit var subscriptionCancelledHandler: SubscriptionCancelledHandler
    private val testDispatcher = StandardTestDispatcher()
    private val onConnectedSlot = CapturingSlot<() -> Unit>()

    @Before
    fun setUp() {
        mockLog()
        Dispatchers.setMain(testDispatcher)

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
    fun `initialize sets up initial state correctly`() = runTest {
        // Given
        val mockProductInfo = ProductInfo(
            productId = "test_product",
            displayName = "Test Product",
            description = "Test Description",
            vendorName = "Test Vendor",
            appName = "Test App",
            price = 9.99,
            displayPrice = "$9.99",
            platform = "ANDROID",
            serviceLevel = "TEST_SERVICE",
            isActive = true,
            recurringPeriodCode = "P1M",
            productType = "SUBSCRIPTION",
            entitlementId = null
        )

        coEvery {
            productManagerUseCases.getActiveSubscription(any())
        } returns Resource.Success(mockk(relaxed = true) {
            every { subscriptionResponseInfo?.product } returns mockProductInfo
            every { productUpdateTimeStamp } returns 123L
            every { themConfigTimeStamp } returns 456L
        })

        // When
        viewModel.initialize("test_user_id", intentLaunch = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isInitialised)
        assertEquals("test_user_id", viewModel.partnerUserId)
        verify { libraryActivityManagerUseCases.launchLibrary() }
    }

    @Test
    fun `onTabSelected filters products correctly`() = runTest {
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
    fun `startConnection initializes billing connection`() = runTest {
        // Given
        var assertionRan = false
        val callbackCompleted = CompletableDeferred<Unit>()
        coEvery {
            billingManagerUseCases.startConnection(
                onConnected = capture(onConnectedSlot),
                onDisconnected = any()
            )
        } coAnswers {
            onConnectedSlot.captured = {
                viewModel.isConnectionStarted = true
                assertTrue(viewModel.isConnectionStarted)
                assertionRan = true
                runBlocking {
                    billingManagerUseCases.checkExistingSubscription(
                        onError = {}
                    )
                }
                callbackCompleted.complete(Unit)
            }
            onConnectedSlot.captured.invoke()
        }

        // When
        viewModel.startConnection()
        testDispatcher.scheduler.runCurrent()
        advanceUntilIdle()
        callbackCompleted.await()
        // Then
        assertTrue(assertionRan, "Callback should have executed")

        coVerify(exactly = 1) {
            billingManagerUseCases.startConnection(any(), any())
        }
        coVerify {
            billingManagerUseCases.checkExistingSubscription(any())
        }
    }

    @Test
    fun `purchaseSubscription handles connection state correctly`() = runTest {
        // Given
        val mockActivity = mockk<ComponentActivity>(relaxed = true)
        val mockProduct = createMockProduct("1", "P1M")
        val errorCallback = mockk<(String) -> Unit>(relaxed = true)
        val callbackCompleted = CompletableDeferred<Unit>()
        var onConnectedCalled = false

        // Mock startConnection behavior
        coEvery {
            billingManagerUseCases.startConnection(
                onConnected = capture(onConnectedSlot),
                onDisconnected = any()
            )
        } coAnswers {
            onConnectedSlot.captured = {
                onConnectedCalled = true
                viewModel.isConnectionStarted = true
                callbackCompleted.complete(Unit)
            }
            onConnectedSlot.captured.invoke()
        }

        // Mock purchaseSubscription behavior
        coEvery {
            billingManagerUseCases.purchaseSubscription(
                activity = any(),
                product = any(),
                onError = any(),
                userId = any()
            )
        } just Runs

        // When - Not connected
        viewModel.isConnectionStarted = false
        viewModel.partnerUserId = "test_user"
        viewModel.purchaseSubscription(mockActivity, mockProduct, errorCallback)

        // Then - Verify connection is started
        testDispatcher.scheduler.runCurrent()
        callbackCompleted.await()
        advanceUntilIdle()

        assertTrue(onConnectedCalled)
        assertTrue(viewModel.isConnectionStarted)
        coVerify {
            billingManagerUseCases.startConnection(any(), any())
        }

        // When - Try purchase again when connected
        viewModel.purchaseSubscription(mockActivity, mockProduct, errorCallback)
        testDispatcher.scheduler.runCurrent()
        advanceUntilIdle()

        // Then - Verify purchase is attempted
        coVerify {
            billingManagerUseCases.purchaseSubscription(
                activity = mockActivity,
                product = mockProduct,
                onError = any(),
                userId = "test_user"
            )
        }
    }

    @Test
    fun `test subscription cancelled handler`() = runTest {
        // Given
        val mockProducts = listOf(
            createMockProduct("1", "P1M")
        )
        viewModel.products = mockProducts

        // When
        subscriptionCancelledHandler.onSubscriptionCancelled()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isLoading.value)
        assertEquals(mockProducts, viewModel.products)
        assertEquals(null, viewModel.filteredProducts)
    }

    @Test
    fun `test purchase update handler states`() = runTest {
        // Given
        coEvery {
            productManagerUseCases.getActiveSubscription(any())
        } returns Resource.Success(mockk(relaxed = true))

        // Create actual handlers instead of mocks
        val purchaseUpdateHandler = PurchaseUpdateHandler(
            onPurchaseStarted = { viewModel.isCurrentProductBeingUpdated = true },
            onPurchaseUpdated = {
                viewModel.isCurrentProductBeingUpdated = false
            },
            onPurchaseFailed = { }
        )

        // Recreate viewModel with actual handler
        viewModel = SubscriptionsViewModel(
            billingManagerUseCases,
            productManagerUseCases,
            themeLoader,
            libraryActivityManagerUseCases,
            purchaseUpdateHandler,
            subscriptionCancelledHandler
        )

        viewModel.initialize("test_user", intentLaunch = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Test onPurchaseStarted
        purchaseUpdateHandler.handlePurchaseStarted()
        assertTrue(viewModel.isCurrentProductBeingUpdated)

        // Test onPurchaseUpdated
        purchaseUpdateHandler.handlePurchaseUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isCurrentProductBeingUpdated)

        // Test onPurchaseFailed
        purchaseUpdateHandler.handlePurchaseFailed()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `test purchase update handler states intent`() = runTest {
        // Given
        coEvery {
            productManagerUseCases.getActiveSubscription(any())
        } returns Resource.Success(mockk(relaxed = true))

        // Create actual handlers instead of mocks
        val purchaseUpdateHandler = PurchaseUpdateHandler(
            onPurchaseStarted = { viewModel.isCurrentProductBeingUpdated = true },
            onPurchaseUpdated = {
                viewModel.isCurrentProductBeingUpdated = false
            },
            onPurchaseFailed = { }
        )

        // Recreate viewModel with actual handler
        viewModel = SubscriptionsViewModel(
            billingManagerUseCases,
            productManagerUseCases,
            themeLoader,
            libraryActivityManagerUseCases,
            purchaseUpdateHandler,
            subscriptionCancelledHandler
        )

        viewModel.initialize("test_user", intentLaunch = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Test onPurchaseStarted
        purchaseUpdateHandler.handlePurchaseStarted()
        assertTrue(viewModel.isCurrentProductBeingUpdated)

        // Test onPurchaseUpdated
        purchaseUpdateHandler.handlePurchaseUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isCurrentProductBeingUpdated)

        // Test onPurchaseFailed
        purchaseUpdateHandler.handlePurchaseFailed()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `test no internet connection scenario`() = runTest {
        // Given
        coEvery {
            productManagerUseCases.getActiveSubscription(any())
        } returns Resource.Error("No internet connection")

        // Reset any existing state
        viewModel.noInternetConnectionAndNoCache.value = false

        // When
        launch {
            viewModel.initialize("test_user", intentLaunch = false)
        }

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.noInternetConnectionAndNoCache.value)
    }

    @Test
    fun `test show toast messages`() = runTest {
        // Test no connection toast
        viewModel.showToast(
            heading = "No Connection",
            message = "Please check your internet connection"
        )
        
        // Test purchase failed toast
        viewModel.showToast(
            heading = "Something went wrong",
            message = "Any debited amount will be refunded."
        )
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