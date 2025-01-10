package com.synchronoss.aiap.presentation


import TabOption
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.di.PurchaseUpdateHandler
import com.synchronoss.aiap.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases,
    private val libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
    private val purchaseUpdateHandler: PurchaseUpdateHandler,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : ViewModel() {

    val dialogState = mutableStateOf(false)
    var products: List<ProductInfo>? by mutableStateOf(null)
    var filteredProducts: List<ProductInfo>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var selectedTab:TabOption? by  mutableStateOf(null)
    var isConnectionStarted: Boolean = false
    var selectedPlan: Int by mutableIntStateOf(-1)

    init {
        subscriptionCancelledHandler.onSubscriptionCancelled = {
            viewModelScope.launch {
                products = null
                filteredProducts = null
                initProducts()
            }
        }
        libraryActivityManagerUseCases.launchLibrary()
        purchaseUpdateHandler.onPurchaseUpdated = {
            products = null
            filteredProducts = null
            viewModelScope.launch {
                initProducts()
            }
        }
    }

    fun startConnection() {
        if(!isConnectionStarted){
            CoroutineScope(Dispatchers.IO).launch {
                    billingManagerUseCases.startConnection(
                        {
                            Log.d("Co", "Connected to billing service");
                            isConnectionStarted = true
                            initProducts()
                        },
                        {
                            Log.d("Co", "Failed to connect to billing service");
                        },
                    )
            }
        }
    }

     private fun initProducts(){
        CoroutineScope(Dispatchers.IO).launch {
            val active =  async { productManagerUseCases.getActiveSubscription() }
            val activeSubscriptionResource = active.await()
            if (activeSubscriptionResource is Resource.Success) {
                val activeSubscriptionInfo = activeSubscriptionResource.data
                currentProductId = activeSubscriptionInfo?.subscriptionResponseInfo?.productId
            }
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
         when (val result = productManagerUseCases.getProductsApi()) {
            is Resource.Success -> {
                if(result.data!=null){
                    products = result.data
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

//    suspend fun getProductById(
//        productId: String,
//        onError: (String) -> Unit
//    ) {
//
//        billingManagerUseCases.getProducts(
//            productIds = listOf(productId),
//            onProductsReceived = {
//                products = it
////                onTabSelected(selectedTab)
//            },
//            onSubscriptionFound = { current ->
//                currentProductId = current
//                if(current != null){
//                    products?.findLast { it.productId == currentProductId }?.let {
//                        selectedTab = when {
//                            it.recurringPeriodCode == "P1M" -> TabOption.MONTHLY
//                            it.recurringPeriodCode == "P1Y" -> TabOption.YEARLY
//                            else -> TabOption.WEEKlY
//                        }
//                    }
//
//                }else{
//                    selectedTab = TabOption.YEARLY
//                }
//                onTabSelected(selectedTab)
//            },
//            onError = onError
//        )
//    }

    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        product: ProductInfo,
        onError: (String) -> Unit
    ) {
        billingManagerUseCases.purchaseSubscription(activity, product, onError)
    }
}