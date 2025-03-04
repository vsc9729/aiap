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
import com.synchronoss.aiap.di.PurchaseUpdateHandler
import com.synchronoss.aiap.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.ui.theme.ThemeColors
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * ViewModel responsible for managing subscription-related UI state and business logic.
 * Handles product filtering, theme management, and purchase operations.
 */
@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
    private val themeLoader: ThemeLoader,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : ViewModel() {

    // UI State
    val dialogState = mutableStateOf(false)
    val isLoading = mutableStateOf(true)
    val noInternetConnectionAndNoCache = mutableStateOf(false)
    var isLaunchedViaIntent: Boolean = false

    
    // Product Management
    var partnerUserId: String? = null
    var products: List<ProductInfo>? by mutableStateOf(null)
    var filteredProducts: List<ProductInfo>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var currentProduct: ProductInfo? by mutableStateOf(null)
    var selectedTab: TabOption? by mutableStateOf(null)
    var isConnectionStarted: Boolean by mutableStateOf(false)
    var selectedPlan: Int by mutableIntStateOf(-1)
    
    // Theme Management
    var darkThemeColors: ThemeColors? = null
    var lightThemeColors: ThemeColors? = null
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

    fun initialize(id:String, intentLaunch: Boolean) {
        isLaunchedViaIntent = intentLaunch
        if(!isInitialised){

            isInitialised = true
            isLoading.value = true
            partnerUserId = id
            viewModelScope.launch {

                val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(userId = partnerUserId!!) }
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

                        primary = lightThemeColors!!.primary,
                        secondary = lightThemeColors!!.secondary,
                        background = lightThemeColors!!.background,
                        onPrimary = lightThemeColors!!.textHeading,
                        onSecondary = lightThemeColors!!.textBody,
                        onBackground = lightThemeColors!!.textBodyAlt,
                        surface = lightThemeColors!!.surface,
                        onSurface = lightThemeColors!!.onSurface,
                        outline = lightThemeColors!!.outline,
                        outlineVariant = lightThemeColors!!.outlineVariant,
                        tertiary = lightThemeColors!!.tertiary,
                        onTertiary = lightThemeColors!!.onTertiary

                    )
                    darkThemeColorScheme = darkColorScheme(
                        primary = darkThemeColors!!.primary,
                        secondary = darkThemeColors!!.secondary,
                        background = darkThemeColors!!.background,
                        onPrimary = darkThemeColors!!.textHeading,
                        onSecondary = darkThemeColors!!.textBody,
                        onBackground = darkThemeColors!!.textBodyAlt,
                        surface = darkThemeColors!!.surface,
                        onSurface = darkThemeColors!!.onSurface,
                        outline = darkThemeColors!!.outline,
                        outlineVariant = darkThemeColors!!.outlineVariant,
                        tertiary = darkThemeColors!!.tertiary,
                        onTertiary = darkThemeColors!!.onTertiary
                    )
                    initProducts()
                }else{

                    noInternetConnectionAndNoCache.value = true
                    showToast(
                        heading = "No Connection",
                        message = "Please check your internet connection"
                    )
                }

            }
            // Only runs after subscription check is complete

                


            subscriptionCancelledHandler.onSubscriptionCancelled = {
                viewModelScope.launch {
                    showToast(
                        heading = "Subscription Cancelled",
                        message = "Your subscription has been cancelled"
                    )
                    isLoading.value = true
                    products = null
                    filteredProducts = null

                    initProducts(subscriptionCancelled =  true)
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
                        heading = "Something went wrong",
                        message = "Any debited amount will be refunded."
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
                        Log.d("Co", "Connected to billing service")
                        isConnectionStarted = true
                        viewModelScope.launch {
                            val checkSubscriptionDeferred = async {
                                billingManagerUseCases.checkExistingSubscription(
                                    onError = {
                                        isLoading.value = false
                                        Log.d("Co", "Error checking subscriptions: $it")
                                    }
                                )
                            }
                            checkSubscriptionDeferred.await()
                        }
                    },
                    {
                        isLoading.value = false
                        Log.d("Co", "Failed to connect to billing service")
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
                 val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription(partnerUserId!!) }
                 val activeSubResult = activeSubResultDeferred.await()
                 val checkSubscriptionDeferred = async {
                     billingManagerUseCases.checkExistingSubscription(
                         onError = {
                             isLoading.value = false
                             Log.d("Co", "Error checking subscriptions: $it")
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
        print("onTabSelected: $tab")
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

         when (val result = productManagerUseCases.getProductsApi(lastKnownProductTimestamp)) {
            is Resource.Success -> {
                if(result.data!=null){
                    products = result.data
                    if(currentProduct != null && (products?.contains(currentProduct) != true)){
                        products = products?.plus(currentProduct!!)
                    }
                    val recurringPeriodCode: String = products!!.findLast { it.productId == currentProductId }?.recurringPeriodCode ?: products!!.first().recurringPeriodCode
                    selectedTab =
                        when (recurringPeriodCode) {
                            "P1M" -> TabOption.MONTHLY
                            "P1Y" -> TabOption.YEARLY
                            else -> TabOption.WEEKlY
                        }

                    filteredProducts = products!!.filter {
                        it.recurringPeriodCode.endsWith(recurringPeriodCode.last())
                    }

                }
            }
            is Resource.Error -> {
                showToast(
                    heading = "Error",
                    message = "Failed to load products. Please try again later."
                )
                Log.d("Co", "Failed to fetch products from API")
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
                billingManagerUseCases.purchaseSubscription(activity, product, { error -> 
                    showToast("Purchase Failed", error)
                    onError(error)
                }, userId = partnerUserId!!)
            }

        }

    }
}