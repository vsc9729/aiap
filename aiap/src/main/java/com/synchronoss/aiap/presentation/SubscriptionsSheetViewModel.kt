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
    var products: List<ProductInfo>? by mutableStateOf(null)
    var filteredProducts: List<ProductInfo>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var currentProduct: ProductInfo? by mutableStateOf(null)
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
        // Track initialization event with user profile
//        localyticsManagerUseCases.setUserProfile(id)
//        localyticsManagerUseCases.trackSubscriptionEvent(
//            eventName = "subscription_view_initialized",
//            productId = "",
//            userId = id,
//            additionalParams = mapOf("intent_launch" to intentLaunch.toString())
//        )
        
        isLaunchedViaIntent = intentLaunch
        if((!isInitialised || id != partnerUserId)){
            isInitialised = true
            isLoading.value = true
            partnerUserId = id
            this.apiKey = apiKey
            viewModelScope.launch {

                val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(userId = partnerUserId, apiKey = apiKey) }
                val startConnection =  async { startConnection() }
                startConnection.await()
                val activeSubResult =  activeSubResultDeferred.await()
                if (activeSubResult is Resource.Success) {
//                    if(!isConnectionStarted){
//                        val billing = async { startConnection() }
//                        billing.await()
//                    }
                    lastKnownProductTimestamp = activeSubResult.data?.productUpdateTimeStamp
                    lastKnownThemeTimestamp = activeSubResult.data?.themConfigTimeStamp
                    currentProductId = activeSubResult.data?.subscriptionResponseInfo?.product?.productId
                    currentProduct = activeSubResult.data?.subscriptionResponseInfo?.product
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
                }else{

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
                    products = null
                    filteredProducts = null

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
                    showToast(
                        heading = context.getString(R.string.purchase_failed_title),
                        message = context.getString(R.string.purchase_failed_message)
                    )
                }
            }

        }

    }

     fun startConnection() {
        if(!isConnectionStarted){
            viewModelScope.launch {
                billingManagerUseCases.startConnection(
                    {
                        LogUtils.d(TAG, context.getString(R.string.billing_connected))
                        isConnectionStarted = true
                        viewModelScope.launch {
                            val checkSubscriptionDeferred = async {
                                billingManagerUseCases.checkExistingSubscription(
                                    onError = {
                                        isLoading.value = false
                                        LogUtils.d(TAG, context.getString(R.string.billing_check_error, it))
                                    }
                                )
                            }
                            checkSubscriptionDeferred.await()
                        }
                    },
                    {
                        isLoading.value = false
                        LogUtils.d(TAG, context.getString(R.string.billing_connection_failed))
                    }
                )
            }
        }
    }

     private suspend fun initProducts(purchaseUpdate: Boolean = false, subscriptionCancelled: Boolean = false) {
         if(purchaseUpdate || subscriptionCancelled){
//             products = null
//             filteredProducts = null
//             selectedPlan = -1
             viewModelScope.launch {
                 val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(partnerUserId, apiKey) }
                 val activeSubResult = activeSubResultDeferred.await()
                 val checkSubscriptionDeferred = async {
                     billingManagerUseCases.checkExistingSubscription(
                         onError = {
                             isLoading.value = false
                             LogUtils.d(TAG, context.getString(R.string.products_fetch_failed))
                         }
                     )
                 }

                 // Wait for the check to complete
                 checkSubscriptionDeferred.await()
                 currentProductId = activeSubResult.data?.subscriptionResponseInfo?.product?.productId
                 currentProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                 fetchAndLoadProducts()
             }
         }else{
             fetchAndLoadProducts()
         }




    }

    fun  onTabSelected(tab: TabOption?) {
        selectedPlan = -1
        selectedTab = tab
        if (selectedTab == TabOption.MONTHLY){
            filteredProducts = products?.filter { product ->
                product.recurringPeriodCode.endsWith("M")
            }
        }else if(selectedTab == TabOption.YEARLY){
            filteredProducts = products?.filter { product ->
                product.recurringPeriodCode.endsWith("Y")
            }
        }else {
            filteredProducts = products?.filter { product ->
                product.recurringPeriodCode.endsWith("W")
            }
        }
    }

    private suspend fun fetchAndLoadProducts(purchaseUpdate: Boolean = false) {

         when (val result = productManagerUseCases.getProductsApi(lastKnownProductTimestamp, apiKey)) {
            is Resource.Success -> {
                if(result.data!=null){
                    products = result.data
                    if(currentProduct != null && (products?.contains(currentProduct) != true)){
                        products = products?.plus(currentProduct!!)
                    }
                    val recurringPeriodCode: String = products?.findLast { it.productId == currentProductId }?.recurringPeriodCode ?: products?.first()!!.recurringPeriodCode
                    selectedTab =
                        when (recurringPeriodCode) {
                            "P1M" -> TabOption.MONTHLY
                            "P1Y" -> TabOption.YEARLY
                            else -> TabOption.WEEKlY
                        }

                    filteredProducts = products?.filter {
                        it.recurringPeriodCode.endsWith(recurringPeriodCode.last())
                    }

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
        product: ProductInfo,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (!isConnectionStarted) {
                startConnection()
            } else {
                billingManagerUseCases.purchaseSubscription(
                    activity, 
                    product, 
                    { error -> 
                        showToast(
                            heading = context.getString(R.string.purchase_failed),
                            message = error
                        )
                        onError(error)
                    }, 
                    userId = partnerUserId,
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