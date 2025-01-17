package com.synchronoss.aiap.presentation


import TabOption
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
    private val themeLoader: ThemeLoader,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    private val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : ViewModel() {

    val dialogState = mutableStateOf(false)
    var products: List<ProductInfo>? by mutableStateOf(null)
    var filteredProducts: List<ProductInfo>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var currentProduct: ProductInfo? by mutableStateOf(null)
    var selectedTab:TabOption? by  mutableStateOf(null)
    var isConnectionStarted: Boolean = false
    var selectedPlan: Int by mutableIntStateOf(-1)
    var darkThemeColors: ThemeColors? = null
    var lightThemeColors: ThemeColors? = null
    var lightThemeLogoUrl:String? = null
    var darkThemeLogoUrl:String? = null
    var finalLogoUrl:String? = null
    var lightThemeColorScheme: ColorScheme? = null
    var darkThemeColorScheme: ColorScheme? = null
    private var lastKnownProductTimestamp: Long? = null
    private var lastKnownThemeTimestamp: Long? = null
    var isPurchaseOngoing: Boolean = false





    init {


        CoroutineScope(Dispatchers.IO).launch {
            val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription() }
            val activeSubResult =  activeSubResultDeferred.await()
            if (activeSubResult is Resource.Success) {
                lastKnownProductTimestamp = activeSubResult.data?.productUpdateTimeStamp
                lastKnownThemeTimestamp = activeSubResult.data?.themConfigTimeStamp
                currentProductId = activeSubResult.data?.subscriptionResponseInfo?.product?.productId
                currentProduct = activeSubResult.data?.subscriptionResponseInfo?.product
                val theme =  async { themeLoader.loadTheme(lastKnownThemeTimestamp) }
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
            }
            
        }



        subscriptionCancelledHandler.onSubscriptionCancelled = {
            viewModelScope.launch {
                products = null
                filteredProducts = null
                initProducts(subscriptionCancelled =  true)
            }
        }
        libraryActivityManagerUseCases.launchLibrary()
        purchaseUpdateHandler.onPurchaseStarted = {
            products = null
            filteredProducts = null
            isPurchaseOngoing = false
        }
        purchaseUpdateHandler.onPurchaseUpdated = {
            viewModelScope.launch {
                initProducts(purchaseUpdate =  true)
            }
        }
    }

    fun startConnection() {
        if(!isConnectionStarted){
            CoroutineScope(Dispatchers.IO).launch {
                billingManagerUseCases.startConnection(
                    {
                        Log.d("Co", "Connected to billing service")
                        isConnectionStarted = true
                        CoroutineScope(Dispatchers.IO).launch {
                            // Create async operation and wait for it
                            val checkSubscriptionDeferred = async {
                                billingManagerUseCases.checkExistingSubscription(
                                    onError = {
                                        Log.d("Co", "Error checking subscriptions: $it")
                                    }
                                )
                            }

                            // Wait for the check to complete
                            checkSubscriptionDeferred.await()

                            // Only runs after subscription check is complete
                            initProducts()
                        }
                    },
                    {
                        Log.d("Co", "Failed to connect to billing service")
                    }
                )
            }
        }
    }

     private suspend fun initProducts(purchaseUpdate: Boolean = false, subscriptionCancelled: Boolean = false) {
         if(purchaseUpdate || subscriptionCancelled){
             products = null
             filteredProducts = null
             CoroutineScope(Dispatchers.IO).launch {
                 val activeSubResultDeferred = async { productManagerUseCases.getActiveSubscription() }
                 val activeSubResult = activeSubResultDeferred.await()
                 val checkSubscriptionDeferred = async {
                     billingManagerUseCases.checkExistingSubscription(
                         onError = {
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

    private suspend fun fetchAndLoadProducts() {
         when (val result = productManagerUseCases.getProductsApi(lastKnownProductTimestamp)) {
            is Resource.Success -> {
                if(result.data!=null){
                    products = result.data
                    if(currentProduct != null && (products?.contains(currentProduct) != true)){
                        products = products?.plus(currentProduct!!)
                    }
                    val recurringPeriodCode: String = products!!.findLast { it.productId == currentProductId }?.recurringPeriodCode ?: "P1Y"
                    selectedTab = if (currentProductId ==null) TabOption.YEARLY else {
                        when (recurringPeriodCode) {
                            "P1M" -> TabOption.MONTHLY
                            "P1Y" -> TabOption.YEARLY
                            else -> TabOption.WEEKlY
                        }
                    }
                    filteredProducts = products!!.filter {
                        it.recurringPeriodCode.endsWith(recurringPeriodCode.last())
                    }

                }
            }
            is Resource.Error -> {
                Log.d("Co", "Failed to fetch products from API")
            }
         }
    }

    fun purchaseSubscription(
        activity: ComponentActivity,
        product: ProductInfo,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            billingManagerUseCases.purchaseSubscription(activity, product, onError)
        }
    }
}