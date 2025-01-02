package com.synchronoss.aiap.presentation


import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val billingManagerUseCases: BillingManagerUseCases,
    private val productManagerUseCases: ProductManagerUseCases
) : ViewModel() {
    val dialogState = mutableStateOf(false)
    var products: List<ProductDetails>? by mutableStateOf(null)
    var selectedProductId: String? = null

     var selectedPlan: Int by mutableIntStateOf(-1
     )



    suspend fun startConnection(productIds: List<String>) {
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
            onProductsReceived = { products = it },
            onSubscriptionFound = { selectedProductId = it },
            onError = onError
        )
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
