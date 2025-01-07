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
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
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
    private val purchaseUpdateHandler: PurchaseUpdateHandler,
) : ViewModel() {

    init {
        purchaseUpdateHandler.onPurchaseUpdated = {
            viewModelScope.launch {
                products = null
                fetchAndLoadProducts()
            }
        }

    }
    val dialogState = mutableStateOf(false)
    var products: List<ProductDetails>? by mutableStateOf(null)
    var filteredProducts: List<ProductDetails>? by mutableStateOf(null)
    var currentProductId: String? by mutableStateOf(null)
    var selectedTab:TabOption? by  mutableStateOf(null)
     var isConnectionStarted: Boolean = false

     var selectedPlan: Int by mutableIntStateOf(-1
     )



    suspend fun startConnection() {
        if(!isConnectionStarted){
            isConnectionStarted = true
            billingManagerUseCases.startConnection(
                {
                    viewModelScope.launch {
                        fetchAndLoadProducts()
                    }
                },
                {
                    Log.d("Co", "Failed to connect to billing service");
                },
            )
        }

    }


    private fun getProductBillingPeriod(product: ProductDetails, billingPeriod: String ): Boolean? {
        return product.subscriptionOfferDetails
            ?.filter { it.offerId == null }
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.lastOrNull()
            ?.billingPeriod?.contains(billingPeriod)
    }
    fun  onTabSelected(tab: TabOption?) {
        print("onTabSelected: $tab")
        selectedPlan = -1
        selectedTab = tab
        if (selectedTab == TabOption.MONTHLY){
            filteredProducts = products?.filter { product ->
                    getProductBillingPeriod(product, "P1M") == true
            }
        }else if(selectedTab == TabOption.YEARLY){
            filteredProducts = products?.filter { product ->
                getProductBillingPeriod(product, "P1Y") == true
            }
        }else {
            filteredProducts = products?.filter { product ->
                getProductBillingPeriod(product, "P1W") == true
            }
        }
    }

    private suspend fun fetchAndLoadProducts() {
         when (val result = productManagerUseCases.getProductsApi()) {
            is Resource.Success -> {
                if(result.data!=null){
                    getProducts(result.data.map {  productInfo -> productInfo.productName
                    }) {
                        error ->
                        Log.d("Co", "Failed to fetch products from billing: $error")
                    }
                }

            }
            is Resource.Error -> {
                Log.d("Co", "Failed to fetch products from API")
            }
         }

//        if (productIds.isNotEmpty()) {
//            getProducts(productIds, onError = {
//                Log.d("Co", "Failed to fetch products from billing")
//            })
//        }
    }

    suspend fun getProducts(
        productIds: List<String>,
        onError: (String) -> Unit
    ) {

        billingManagerUseCases.getProducts(
            productIds = productIds,
            onProductsReceived = {
                products = it
//                onTabSelected(selectedTab)
            },
            onSubscriptionFound = { current ->
                currentProductId = current
                if(current != null){
                    products?.findLast { it.productId == currentProductId }?.let {
                        selectedTab = when {
                            getProductBillingPeriod(it, "P1M") == true -> TabOption.MONTHLY
                            getProductBillingPeriod(it, "P1Y") == true -> TabOption.YEARLY
                            else -> TabOption.WEEKlY
                        }
                    }

                }else{
                    selectedTab = TabOption.YEARLY
                }
                onTabSelected(selectedTab)
                
                print("currentProductId: $currentProductId")


            },
            onError = onError)


    }

    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        product: ProductDetails,
        onError: (String) -> Unit
    ) {
        billingManagerUseCases.purchaseSubscription(activity, product, onError)
    }

//    fun updateCurrentPlan(product: ProductDetails){
//        _currentPlan.value = CurrentSelected(product);
//    }
}

//data class CurrentSelected(
//    val product: ProductDetails? = null,
//)
