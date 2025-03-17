package com.synchronoss.aiap.presentation.viewmodels

// Android imports
import android.content.Context
import androidx.activity.ComponentActivity

// Compose imports
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Lifecycle imports
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// Billing imports
import com.android.billingclient.api.ProductDetails

// Dagger imports
import javax.inject.Inject

// Kotlin coroutines imports
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Application imports
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.data.repository.billing.GlobalBillingConfig
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.analytics.SegmentAnalyticsUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.presentation.subscriptions.ui.TabOption
import com.synchronoss.aiap.ui.theme.ThemeColors
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.LogUtils
import com.synchronoss.aiap.utils.NetworkConnectionListener
import com.synchronoss.aiap.utils.Resource
import com.synchronoss.aiap.utils.ToastService

/**
 * ViewModel responsible for managing subscription-related UI state and business logic.
 * Handles product filtering, theme management, and purchase operations.
 */
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
    private val themeLoader: ThemeLoader,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler,
    private val segmentAnalyticsUseCases: SegmentAnalyticsUseCases,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SubscriptionsSheetVM"
    }

    // Create the ToastService and NetworkConnectionListener
    private val coroutineScope = viewModelScope
    private val toastService = ToastService(context, coroutineScope)
    private val networkConnectionListener = NetworkConnectionListener(context, toastService)

    //region UI State Properties
    /** Loading and dialog states */
    val isLoading = mutableStateOf(true)
    val dialogState = mutableStateOf(false)
    val noInternetConnectionAndNoCache = mutableStateOf(false)
    var isLaunchedViaIntent: Boolean = false
    var isInitialised: Boolean by mutableStateOf(false)
    var isConnectionStarted: Boolean by mutableStateOf(false)
    
    /** Tab and selection state */
    var selectedTab: TabOption? by mutableStateOf(null)
    var selectedPlan: Int by mutableIntStateOf(-1)
    var isCurrentProductBeingUpdated: Boolean by mutableStateOf(false)
    
    /** Toast state reference from ToastService */
    val toastState get() = toastService.toastState
    //endregion

    //region User and Product Data Properties
    /** User identification */
    lateinit var apiKey: String
    lateinit var partnerUserId: String
    lateinit var userUUID: String
    var isIosPlatform: Boolean by mutableStateOf(false)
    
    /** Product information */
    var products: List<ProductInfo>? by mutableStateOf(null)
    var productDetails: List<ProductDetails>? by mutableStateOf(null)
    var filteredProductDetails: List<ProductDetails>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var currentProduct: ProductInfo? by mutableStateOf(null)
    var activeProduct: ProductInfo? by mutableStateOf(null)
    var baseServiceLevel: String? by mutableStateOf(null)
    var currentProductDetails: ProductDetails? by mutableStateOf(null)
    var unacknowledgedProductDetails: ProductDetails? by mutableStateOf(null)
    
    /** Tracking information */
    private var lastKnownProductTimestamp: Long? = null
    private var lastKnownThemeTimestamp: Long? = null
    //endregion

    //region Theme Properties
    /** Theme colors */
    lateinit var darkThemeColors: ThemeColors
    lateinit var lightThemeColors: ThemeColors
    var lightThemeLogoUrl: String? = null
    var darkThemeLogoUrl: String? = null
    var finalLogoUrl: String? = null
    
    /** Theme color schemes */
    var lightThemeColorScheme: ColorScheme? by mutableStateOf(null)
    var darkThemeColorScheme: ColorScheme? by mutableStateOf(null)
    //endregion

    //region Toast Management
    /**
     * Shows a toast with string heading and message.
     * Delegates to the ToastService.
     */
    fun showToast(
        heading: String,
        message: String,
        isSuccess: Boolean = false,
        isPending: Boolean = false,
        formatArgs: Any? = null
    ) {
        toastService.showToast(heading, message, isSuccess, isPending, formatArgs)
    }

    /**
     * Hides the currently displayed toast.
     * Delegates to the ToastService.
     */
    fun hideToast() {
        toastService.hideToast()
    }
    //endregion

    //region Initialization
    /**
     * Initializes the ViewModel with user identification and sets up handlers
     */
    fun initialize(id: String, apiKey: String, intentLaunch: Boolean, activity: ComponentActivity) {
        isLaunchedViaIntent = intentLaunch
        if (!isInitialised || id != GlobalBillingConfig.partnerUserId) {
            isInitialised = true
            isLoading.value = true
            GlobalBillingConfig.partnerUserId = id
            GlobalBillingConfig.apiKey = apiKey

            viewModelScope.launch {
                try {
                    // Initialize LogUtils first
                    LogUtils.initialize(context)
                    LogUtils.clearLogs()
                    
                    // Initialize network connection listener first
                    initNetworkListener()

                    // Initialize Segment Analytics
                    segmentAnalyticsUseCases.initialize()

                    //load theme
                    val loadTheme = async { loadTheme() }
                    loadTheme.await()
                    
                    // First establish billing connection
                    val startConnectionDeferred = async { startConnection() }
                    startConnectionDeferred.await()

                    // Get active subscription information
                    var activeSubResultDeferred = async {
                        productManagerUseCases.getActiveSubscription(userId = id, apiKey = apiKey)
                    }
                    var activeSubResult = activeSubResultDeferred.await()
                    userUUID = activeSubResult.data?.userUUID!!
                    GlobalBillingConfig.userUUID = userUUID

                    // Handle any unacknowledged purchases first
                    val purchaseHandledDeferred = async {
                        billingManagerUseCases.handleUnacknowledgedPurchases(
                            onError = {
                                LogUtils.d(TAG, context.getString(R.string.billing_check_error, it))
                            }
                        )
                    }
                    val purchaseHandledResult = purchaseHandledDeferred.await()

                    if(purchaseHandledResult){
                        activeSubResultDeferred = async {
                            productManagerUseCases.getActiveSubscription(userId = id, apiKey = apiKey)
                        }
                        activeSubResult = activeSubResultDeferred.await()
                    }


                    if (activeSubResult is Resource.Success) {
                        val checkSubscriptionDeferred = async {
                            billingManagerUseCases.checkExistingSubscription(
                                onError = {
                                    isLoading.value = false
                                    LogUtils.d(TAG, context.getString(R.string.products_fetch_failed))
                                }
                            )
                        }
                        unacknowledgedProductDetails =  checkSubscriptionDeferred.await()
                        
                        // Set up subscription data
                        activeProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                        baseServiceLevel = activeSubResult.data?.baseServiceLevel!!
                        lastKnownProductTimestamp = activeSubResult.data?.productUpdateTimeStamp
                        lastKnownThemeTimestamp = activeSubResult.data?.themConfigTimeStamp
                        currentProductId = activeSubResult.data?.subscriptionResponseInfo?.product?.productId
                        currentProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                        userUUID = activeSubResult.data?.userUUID!! 
                        GlobalBillingConfig.userUUID = userUUID
                        isIosPlatform = activeSubResult.data!!.subscriptionResponseInfo?.platform?.equals("IOS", ignoreCase = true) ?: false

                        // Initialize products
                        initProducts()
                    } else {
                        noInternetConnectionAndNoCache.value = true
                        showToast(
                            heading = context.getString(R.string.no_connection_title) ,
                            message = context.getString(R.string.no_connection_message)
                        )
                    }
                } catch (e: Exception) {
                    LogUtils.e(TAG, "Initialization failed", e)
                    isLoading.value = false
                }
            }

            // Setup event handlers
            setupEventHandlers()
            
            libraryActivityManagerUseCases.launchLibrary(activity)
        }
    }
    
    /**
     * Initializes the network connection listener
     */
    private suspend fun initNetworkListener() {
        try {
            // Register the network connection listener
            val isConnected = networkConnectionListener.register()
            
            LogUtils.d(TAG, "Network listener initialized, connected: $isConnected")
            
            // If not connected, show a toast
            if (!isConnected) {
                showToast(
                    heading = context.getString(R.string.no_connection_title),
                    message = context.getString(R.string.no_connection_message)
                )
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to initialize network listener", e)
        }
    }

    /**
     * Sets up various event handlers for purchase and subscription events
     */
    public fun setupEventHandlers() {
        // Subscription cancelled handler
        subscriptionCancelledHandler.onSubscriptionCancelled = {
            viewModelScope.launch {
                val initialCurrentProduct = currentProduct
                isLoading.value = true
                val init = async { initProducts(subscriptionCancelled = true) }
                init.await()
                if (initialCurrentProduct != currentProduct && currentProduct == null) {
                    showToast(
                        heading = context.getString(R.string.subscription_cancelled_title),
                        message = context.getString(R.string.subscription_cancelled_message),
                    )
                }
            }
        }

        // Purchase update handlers
        purchaseUpdateHandler.onPurchaseStarted = {
            isCurrentProductBeingUpdated = true
        }

        purchaseUpdateHandler.onPurchaseUpdated = {
            viewModelScope.launch {
                selectedPlan = -1
                // Track successful purchase
                trackPurchaseSuccess()
                val init = async { initProducts(purchaseUpdate = true) }
                init.await()
                isCurrentProductBeingUpdated = false
                showToast(
                    heading = context.getString(R.string.purchase_completed_title),
                    message = context.getString(R.string.purchase_completed_message),
                    formatArgs = currentProductDetails?.name,
                    isSuccess = true,
                )
            }
        }

        purchaseUpdateHandler.isLaunchedViaIntent = isLaunchedViaIntent
        
        purchaseUpdateHandler.onPurchaseFailed = {
            viewModelScope.launch {
                val init = async { initProducts(purchaseUpdate = true) }
                init.await()
                isCurrentProductBeingUpdated = false
                showToast(
                    heading = context.getString(R.string.purchase_pending_title),
                    message = context.getString(R.string.purchase_pending_message),
                    formatArgs = currentProductDetails?.name,
                    isPending = true,
                )
            }
        }

        purchaseUpdateHandler.onPurchaseStopped = {
            viewModelScope.launch {
                isCurrentProductBeingUpdated = false
            }
        }
    }

    /**
     * Loads theme colors and creates color schemes
     */
    private fun loadTheme() {
        viewModelScope.launch {
            val theme = async { themeLoader.loadTheme() }
            theme.await()

            // Set up light theme
            lightThemeColors = themeLoader.getThemeColors().themeColors
            lightThemeLogoUrl = themeLoader.getThemeColors().logoUrl

            // Set up dark theme
            darkThemeColors = themeLoader.getDarkThemeColors().themeColors
            darkThemeLogoUrl = themeLoader.getDarkThemeColors().logoUrl

            finalLogoUrl = lightThemeLogoUrl

            // Create light color scheme
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

            // Create dark color scheme
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
        }
    }
    //endregion

    //region Billing Connection
    /**
     * Establishes connection with the billing service
     */
    suspend fun startConnection() {
        if (!isConnectionStarted) {
            try {
                billingManagerUseCases.startConnection().await()
                LogUtils.d(TAG, context.getString(R.string.billing_connected))
                isConnectionStarted = true
            } catch (e: Exception) {
                isLoading.value = false
                LogUtils.d(TAG, context.getString(R.string.billing_connection_failed))
                throw e
            }
        }
    }
    //endregion

    //region Product Management
    /**
     * Initializes product data, optionally handling purchase updates or subscription cancellations
     */
    public suspend fun initProducts(purchaseUpdate: Boolean = false, subscriptionCancelled: Boolean = false) {
        if(purchaseUpdate || subscriptionCancelled){
            viewModelScope.launch {
                val activeSubResultDeferred = async { 
                    productManagerUseCases.getActiveSubscription(GlobalBillingConfig.partnerUserId, GlobalBillingConfig.apiKey) 
                }
                val activeSubResult = activeSubResultDeferred.await()
                if(activeSubResult is Resource.Success){
                    activeProduct = activeSubResult.data?.subscriptionResponseInfo?.product

                    baseServiceLevel = activeSubResult.data?.baseServiceLevel!!
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
                    unacknowledgedProductDetails =  checkSubscriptionDeferred.await()
                    fetchAndLoadProducts()
                }
            }
        } else {
            fetchAndLoadProducts()
        }
    }

    /**
     * Fetches product information from API and loads it into the UI
     */
    public suspend fun fetchAndLoadProducts(purchaseUpdate: Boolean = false) {
        when (val result = productManagerUseCases.getProductsApi(lastKnownProductTimestamp, GlobalBillingConfig.apiKey)) {
            is Resource.Success -> {
                products = result.data
                // Get product IDs from ProductInfo objects
                val productIds = products?.map { it.productId } ?: emptyList()

                // Fetch product details from Google Play Billing Library
                productDetails = billingManagerUseCases.getProductDetails(productIds, onError = { error ->
                    LogUtils.d(TAG, "Failed to fetch product details: $error")
                })
                
                // Handle iOS platform special case
                handleIosPlatformProducts()
                
                // Initialize tabs if not already selected
                initializeTabsIfNeeded()
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

    /**
     * Handles iOS platform product mapping to Android products
     */
    private fun handleIosPlatformProducts() {
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
        } else {
            currentProductDetails = productDetails?.firstOrNull { it.productId == currentProductId }
        }
    }

    /**
     * Initializes tab selection based on current product or available periods
     */
    public fun initializeTabsIfNeeded() {
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

    /**
     * Handles tab selection and filters products accordingly
     */
    fun onTabSelected(tab: TabOption?) {
        selectedPlan = -1
        selectedTab = tab

        // Determine the required billing period suffix based on the selected tab.
        val billingSuffix = when (selectedTab) {
            TabOption.MONTHLY -> "M"
            TabOption.YEARLY -> "Y"
            else -> "W"
        }

        // Filter productDetails using the computed billingSuffix.
        filteredProductDetails = productDetails?.filter { details ->
            details.subscriptionOfferDetails?.last()?.pricingPhases
                ?.pricingPhaseList?.firstOrNull()?.billingPeriod?.endsWith(billingSuffix) == true
        }
    }

    //endregion

    //region Purchase Processing
    /**
     * Tracks successful purchase in analytics
     */
    public fun trackPurchaseSuccess() {
        currentProductDetails?.let { details ->
            segmentAnalyticsUseCases.track(
                eventName = "subscription_purchase_success",
                properties = mapOf(
                    "product_id" to details.productId,
                    "user_id" to userUUID,
                    "product_name" to (details.name ?: ""),
                    "price" to (details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "")
                )
            )
        }
    }

    /**
     * Initiates the purchase of a subscription
     */
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
                // Track purchase attempt
                trackPurchaseAttempt(productDetails)

                // Check for existing subscriptions before proceeding
                val existingSubscriptionDeferred = async {
                    billingManagerUseCases.checkExistingSubscription(
                        onError = { error ->
                            // Track error
                            trackPurchaseError(productDetails, error)
                            showToast(
                                heading = context.getString(R.string.error_title),
                                message = error
                            )
                            onError(error)
                        })
                }

                unacknowledgedProductDetails =  existingSubscriptionDeferred.await()

                billingManagerUseCases.purchaseSubscription(
                    activity, 
                    productDetails, 
                    { error -> 
                        // Track error
                        trackPurchaseError(productDetails, error)
                        showToast(
                            heading = context.getString(R.string.purchase_failed),
                            message = error
                        )
                        onError(error)
                    },
                    userId = userUUID,
                    apiKey = GlobalBillingConfig.apiKey
                )
            }
        }
    }

    /**
     * Tracks purchase attempt in analytics
     */
    public fun trackPurchaseAttempt(productDetails: ProductDetails) {
        segmentAnalyticsUseCases.track(
            eventName = "subscription_purchase_attempt",
            properties = mapOf(
                "product_id" to productDetails.productId,
                "user_id" to userUUID,
                "product_name" to (productDetails.name ?: ""),
                "price" to (productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "")
            )
        )
    }

    /**
     * Tracks purchase error in analytics
     */
    public fun trackPurchaseError(productDetails: ProductDetails, error: String) {
        segmentAnalyticsUseCases.track(
            eventName = "subscription_purchase_error",
            properties = mapOf(
                "product_id" to productDetails.productId,
                "user_id" to userUUID,
                "error" to error
            )
        )
    }
    //endregion

    /**
     * Cleans up resources when ViewModel is cleared
     */
    override fun onCleared() {
        // Unregister network listener
        networkConnectionListener.unregister()
        
        libraryActivityManagerUseCases.cleanup()
        super.onCleared()
    }

    /**
     * Clears the ViewModel state when bottom sheet is dismissed
     */
    fun clearState() {
        // Unregister network listener
        networkConnectionListener.unregister()
        
        isLoading.value = true
        dialogState.value = false
        noInternetConnectionAndNoCache.value = false
        isInitialised = false
        isConnectionStarted = false
        selectedTab = null
        selectedPlan = -1
        isCurrentProductBeingUpdated = false
        products = null
        productDetails = null
        filteredProductDetails = null
        currentProductId = null
        currentProduct = null
        activeProduct = null
        baseServiceLevel = null
        currentProductDetails = null
        lastKnownProductTimestamp = null
        lastKnownThemeTimestamp = null
    }
}

