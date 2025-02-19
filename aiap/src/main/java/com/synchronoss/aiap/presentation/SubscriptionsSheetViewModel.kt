package com.synchronoss.aiap.presentation


import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synchronoss.aiap.ui.theme.ThemeColors
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.synchronoss.aiap.R
import android.content.Context
import androidx.compose.runtime.remember
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import dagger.hilt.android.qualifiers.ApplicationContext
import com.synchronoss.aiap.core.domain.usecases.analytics.LocalyticsManagerUseCases
import com.synchronoss.aiap.utils.LogUtils
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.presentation.subscriptions.TabOption

/**
 * ViewModel responsible for managing subscription-related UI state and business logic.
 * Handles product filtering, theme management, and purchase operations.
 */
@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
//    private val localyticsManagerUseCases: LocalyticsManagerUseCases,
    private val themeLoader: ThemeLoader,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI State
    val dialogState = mutableStateOf(false)
    val isLoading = mutableStateOf(true)
    val noInternetConnectionAndNoCache = mutableStateOf(false)
    var isLaunchedViaIntent: Boolean = false

    
    // Product Management
    lateinit var apiKey: String
    lateinit var partnerUserId: String
    lateinit var userUUID: String
    var products: List<ProductInfo>? by mutableStateOf(null)
    var productDetails: List<ProductDetails>? by mutableStateOf(null)
    var filteredProductDetails: List<ProductDetails>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var currentProduct: ProductInfo? by mutableStateOf(null)
    var activeProduct: ProductInfo? by mutableStateOf(null)
    var currentProductDetails: ProductDetails? by mutableStateOf(null)
    var selectedTab: TabOption? by mutableStateOf(null)
    var isConnectionStarted: Boolean by mutableStateOf(false)
    var selectedPlan: Int by mutableIntStateOf(-1)
    
    // Theme Management
    lateinit var darkThemeColors: ThemeColors
    lateinit var lightThemeColors: ThemeColors
    var lightThemeLogoUrl: String? = null
    var darkThemeLogoUrl: String? = null
    var finalLogoUrl: String? = null
    var lightThemeColorScheme: ColorScheme? by mutableStateOf(null)
    var darkThemeColorScheme: ColorScheme? by mutableStateOf(null)
    
    // State Management
    var isInitialised: Boolean by mutableStateOf(false)
    var isCurrentProductBeingUpdated: Boolean by mutableStateOf(false)
    var isIosPlatform: Boolean by mutableStateOf(false)
    private var lastKnownProductTimestamp: Long? = null
    private var lastKnownThemeTimestamp: Long? = null

    // Toast Handling
    private var toastJob: Job? = null
    var toastState by mutableStateOf(ToastState())
        private set

    fun showToast(headingResId: Int, messageResId: Int) {
    toastJob?.cancel()
    toastState = ToastState(
        isVisible = true,
        headingResId = headingResId,
        messageResId = messageResId
    )
    toastJob = viewModelScope.launch {
        delay(3000)
        hideToast()
    }
}

    // Overload for string messages
    fun showToast(heading: String, message: String) {
        toastJob?.cancel()
        toastState = ToastState(
            isVisible = true,
            heading = heading,
            message = message
        )
        toastJob = viewModelScope.launch {
            delay(3000)
            hideToast()
        }
    }

    fun hideToast() {
        toastJob?.cancel()
        toastState = toastState.copy(isVisible = false)
    }

    fun initialize(id:String, apiKey:String,  intentLaunch: Boolean) {
        isLaunchedViaIntent = intentLaunch
        if((!isInitialised || id != partnerUserId)){
            isInitialised = true
            isLoading.value = true
            partnerUserId = id
            this.apiKey = apiKey
            viewModelScope.launch {
                val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(userId = partnerUserId, apiKey = apiKey) }
                val startConnectionDeferred = async { startConnection() }
                val activeSubResult = activeSubResultDeferred.await()
                
                if (activeSubResult is Resource.Success) {
                    activeProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                    startConnectionDeferred.await()
                    lastKnownProductTimestamp = activeSubResult.data?.productUpdateTimeStamp
                    lastKnownThemeTimestamp = activeSubResult.data?.themConfigTimeStamp
                    currentProductId = activeSubResult.data?.subscriptionResponseInfo?.product?.productId
                    currentProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                    userUUID = activeSubResult.data?.userUUID!! //Always returns if call succeeds
                    isIosPlatform = activeSubResult.data.subscriptionResponseInfo?.platform?.equals("IOS", ignoreCase = true) ?: false
                    val theme =  async { themeLoader.loadTheme() }
                    theme.await()
                    lightThemeColors = themeLoader.getThemeColors().themeColors
                    lightThemeLogoUrl = themeLoader.getThemeColors().logoUrl;
                    darkThemeColors = themeLoader.getDarkThemeColors().themeColors
                    darkThemeLogoUrl = themeLoader.getDarkThemeColors().logoUrl;

                    finalLogoUrl = lightThemeLogoUrl

                    lightThemeColorScheme = lightColorScheme(

                        primary = lightThemeColors.primary,
                        secondary = lightThemeColors.secondary,
                        background = lightThemeColors.background,
                        onPrimary = lightThemeColors.textHeading,
                        onSecondary = lightThemeColors.textBody,
                        onBackground = lightThemeColors.textBodyAlt,
                        surface = lightThemeColors.surface,
                        onSurface = lightThemeColors.onSurface,
                        outline = lightThemeColors.outline,
                        outlineVariant = lightThemeColors.outlineVariant,
                        tertiary = lightThemeColors.tertiary,
                        onTertiary = lightThemeColors.onTertiary
                    )
                    darkThemeColorScheme = darkColorScheme(
                        primary = darkThemeColors.primary,
                        secondary = darkThemeColors.secondary,
                        background = darkThemeColors.background,
                        onPrimary = darkThemeColors.textHeading,
                        onSecondary = darkThemeColors.textBody,
                        onBackground = darkThemeColors.textBodyAlt,
                        surface = darkThemeColors.surface,
                        onSurface = darkThemeColors.onSurface,
                        outline = darkThemeColors.outline,
                        outlineVariant = darkThemeColors.outlineVariant,
                        tertiary = darkThemeColors.tertiary,
                        onTertiary = darkThemeColors.onTertiary
                    )
                    initProducts()
                } else {
                    noInternetConnectionAndNoCache.value = true
                    showToast(
                        headingResId = R.string.no_connection_title,
                        messageResId = R.string.no_connection_message
                    )
                }
            }
            // Only runs after subscription check is complete

                


            subscriptionCancelledHandler.onSubscriptionCancelled = {
                viewModelScope.launch {
                    val initialCurrentProduct = currentProduct
//                    showToast(
//                        heading = context.getString(R.string.subscription_cancelled_title),
//                        message = context.getString(R.string.subscription_cancelled_message)
//                    )
                    isLoading.value = true
//                    products = null
//                    filteredProductDetails = null

                    val init =  async { initProducts(subscriptionCancelled = true) }
                    init.await()
                    if(initialCurrentProduct != currentProduct && currentProduct == null){
                        showToast(
                            heading = context.getString(R.string.subscription_cancelled_title),
                            message = context.getString(R.string.subscription_cancelled_message)
                        )
                    }
                }
            }
            libraryActivityManagerUseCases.launchLibrary()
            purchaseUpdateHandler.onPurchaseStarted = {
//            isLoading.value = true
                isCurrentProductBeingUpdated = true

//            products = null
//            filteredProducts = null

            }
            purchaseUpdateHandler.onPurchaseUpdated = {
                viewModelScope.launch {
                    selectedPlan = -1
                    val init = async { initProducts(purchaseUpdate = true) }
                    init.await()
                    isCurrentProductBeingUpdated = false
                }
            }
            purchaseUpdateHandler.isLaunchedViaIntent = isLaunchedViaIntent
            purchaseUpdateHandler.onPurchaseFailed = {
                viewModelScope.launch {
                    val init = async { initProducts(purchaseUpdate = true) }
                    init.await()
                    isCurrentProductBeingUpdated = false
                    showToast(
                        heading = context.getString(R.string.purchase_failed_title),
                        message = context.getString(R.string.purchase_failed_message)
                    )
                }
            }

            purchaseUpdateHandler.onPurchaseStopped = {
                viewModelScope.launch {
                    isCurrentProductBeingUpdated = false

                }
            }

        }

    }

    suspend fun startConnection() {
        if(!isConnectionStarted) {
            try {
                billingManagerUseCases.startConnection().await()
                LogUtils.d(TAG, context.getString(R.string.billing_connected))
                isConnectionStarted = true

                 billingManagerUseCases.checkExistingSubscription(
                    onError = {
                        isLoading.value = false
                        LogUtils.d(TAG, context.getString(R.string.billing_check_error, it))
                    }
                )
            } catch (e: Exception) {
                isLoading.value = false
                LogUtils.d(TAG, context.getString(R.string.billing_connection_failed))
            }
        }
    }

     private suspend fun initProducts(purchaseUpdate: Boolean = false, subscriptionCancelled: Boolean = false) {
         if(purchaseUpdate || subscriptionCancelled){
             viewModelScope.launch {
                 val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(partnerUserId, apiKey) }
                 val activeSubResult = activeSubResultDeferred.await()
                 if(activeSubResult is Resource.Success){
                     activeProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                     currentProduct = activeProduct
                     currentProductId = currentProduct?.productId
                     val checkSubscriptionDeferred = async {
                         billingManagerUseCases.checkExistingSubscription(
                             onError = {
                                 isLoading.value = false
                                 LogUtils.d(TAG, context.getString(R.string.products_fetch_failed))
                             }
                         )
                     }
                     checkSubscriptionDeferred.await()
                     fetchAndLoadProducts()
                 }

             }
         } else {
             fetchAndLoadProducts()
         }
     }

    fun onTabSelected(tab: TabOption?) {
        selectedPlan = -1
        selectedTab = tab
        when (selectedTab) {
            TabOption.MONTHLY -> {
                // Filter productDetails where billing period ends with "M"
                filteredProductDetails = productDetails?.filter { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true
                }
            }
            TabOption.YEARLY -> {
                // Filter productDetails where billing period ends with "Y"
                filteredProductDetails = productDetails?.filter { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("Y") == true
                }
            }
            else -> {
                // Filter productDetails where billing period ends with "W"
                filteredProductDetails = productDetails?.filter { details ->
                    details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true
                }
            }
        }
    }

    private suspend fun fetchAndLoadProducts(purchaseUpdate: Boolean = false) {
        when (val result = productManagerUseCases.getProductsApi(lastKnownProductTimestamp, apiKey)) {
            is Resource.Success -> {
                products = result.data
                // Get product IDs from ProductInfo objects
                val productIds = products?.map { it.productId } ?: emptyList()

                // Fetch product details from Google Play Billing Library
                productDetails = billingManagerUseCases.getProductDetails(productIds, onError = { error ->
                    showToast(
                        heading = context.getString(R.string.error_title),
                        message = error
                    )
                    LogUtils.d(TAG, "Failed to fetch product details: $error")
                })
                if (isIosPlatform) {
                        // Get the recurring period and service level from iOS subscription
                        val iosRecurringPeriod = activeProduct?.recurringPeriodCode
                        val iosServiceLevel = activeProduct?.serviceLevel
                        // Find matching Android product based on recurring period and service level
                        currentProduct = products?.firstOrNull { product ->
                            product.recurringPeriodCode == iosRecurringPeriod &&
                                    product.serviceLevel == iosServiceLevel
                        }
                        // Set currentProductDetails based on currentProduct if found
                        if (currentProduct != null) {
                            currentProductId = currentProduct?.productId
                            currentProductDetails = productDetails?.firstOrNull { details ->
                                details.productId == currentProductId
                        }
                    }

                }else{
                    currentProductDetails = productDetails?.firstOrNull { it.productId == currentProductId }
                }


//                val currentProduct: ProductInfo? = products?.find { it.productId == currentProductId }
                if(selectedTab == null){
                    if(currentProductDetails != null){
                        selectedTab = when {
                            currentProductDetails?.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true -> TabOption.WEEKlY
                            currentProductDetails?.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true -> TabOption.MONTHLY
                            else -> TabOption.YEARLY
                        }

                        // Filter based on the current product's billing period
                        val currentBillingPeriod = currentProductDetails?.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod
                        if (currentBillingPeriod != null) {
                            filteredProductDetails = productDetails?.filter { details ->
                                details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.last() == currentBillingPeriod.last()
                            }
                        }
                    } else {
                        // If no current product, check available periods in productDetails
                        if (productDetails?.any { details -> 
                            details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true 
                        } == true) {
                            selectedTab = TabOption.WEEKlY
                            filteredProductDetails = productDetails?.filter { details ->
                                details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("W") == true
                            }
                        } else if (productDetails?.any { details ->
                            details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true
                        } == true) {
                            selectedTab = TabOption.MONTHLY
                            filteredProductDetails = productDetails?.filter { details ->
                                details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("M") == true
                            }
                        } else {
                            selectedTab = TabOption.YEARLY
                            filteredProductDetails = productDetails?.filter { details ->
                                details.subscriptionOfferDetails?.last()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith("Y") == true
                            }
                        }
                    }
                    selectedTab?.let { onTabSelected(it) }
                }
            }
            is Resource.Error -> {
                showToast(
                    heading = context.getString(R.string.error_title),
                    message = context.getString(R.string.error_products_message)
                )
                LogUtils.d(TAG, context.getString(R.string.products_fetch_failed))
            }
        }
        isLoading.value = false
    }

    fun purchaseSubscription(
        activity: ComponentActivity,
        productDetails: ProductDetails,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!isConnectionStarted) {
                val startConnectionDeferred = async { startConnection() }
                startConnectionDeferred.await()
            } else {
                // Check for existing subscriptions before proceeding
                val existingSubscriptionDeferred = async {
                    billingManagerUseCases.checkExistingSubscription(
                        onError = { error ->
                            showToast(
                                heading = context.getString(R.string.error_title),
                                message = error
                            )
                            onError(error)
                        })
                }

                existingSubscriptionDeferred.await()

                billingManagerUseCases.purchaseSubscription(
                    activity, 
                    productDetails, 
                    { error -> 
                        showToast(
                            heading = context.getString(R.string.purchase_failed),
                            message = error
                        )
                        onError(error)
                    },
                    userId = userUUID,
                    apiKey = apiKey
                )
            }
        }
    }

    override fun onCleared() {
        libraryActivityManagerUseCases.cleanup()
        super.onCleared()

    }

    companion object {
        private const val TAG = "SubscriptionsSheetVM"
    }
}

