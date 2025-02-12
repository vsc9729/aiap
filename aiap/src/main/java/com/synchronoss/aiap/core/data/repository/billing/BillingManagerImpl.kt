package com.synchronoss.aiap.core.data.repository.billing

import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.di.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response

class BillingManagerImpl(
    context: Context,
    private val productApi: ProductApi,
    private val purchaseUpdateHandler: PurchaseUpdateHandler
) : PurchasesUpdatedListener,
    BillingManager {

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private var currentProduct :Purchase? = null
    private var partnerUserId :String? = null
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override suspend fun startConnection(onConnected: () -> Unit, onDisconnected: () -> Unit) {

             billingClient.startConnection(object : BillingClientStateListener {
                 override fun onBillingSetupFinished(billingResult: BillingResult) {
                     if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                         onConnected()
                     }
                 }

                 override fun onBillingServiceDisconnected() {
                     onDisconnected()
                 }
             })
    }


    override suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productInfo: ProductInfo,
        onError: (String) -> Unit,
        userId: String
    ) {
        partnerUserId = userId

        try {
            val productParams = createProductParams(productInfo)
            queryAndProcessProduct(activity, productParams, onError)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    private fun createProductParams(productInfo: ProductInfo): QueryProductDetailsParams {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productInfo.productId)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        return QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
    }

    private fun queryAndProcessProduct(
        activity: ComponentActivity,
        params: QueryProductDetailsParams,
        onError: (String) -> Unit
    ) {
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                onError(billingResult.debugMessage)
                return@queryProductDetailsAsync
            }

            handleProductDetails(activity, productDetailsList, onError)
        }
    }

    private fun handleProductDetails(
        activity: ComponentActivity,
        productDetailsList: List<ProductDetails>,
        onError: (String) -> Unit
    ) {
        val productDetails:ProductDetails?  = productDetailsList.firstOrNull()
        if(productDetails == null){
            onError("Billing client failed to return product")
        }else{
            productDetailsMap[productDetails.productId] = productDetails
            val productDetailsParams = createProductDetailsParams(productDetails)
            launchBillingFlow(activity, productDetailsParams, onError)
        }
    }

    private fun createProductDetailsParams(product: ProductDetails): List<BillingFlowParams.ProductDetailsParams> {
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken.orEmpty()

        return listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()
        )
    }

    private fun launchBillingFlow(
        activity: ComponentActivity,
        productDetailsParams: List<BillingFlowParams.ProductDetailsParams>,
        onError: (String) -> Unit
    ) {
        val flowParams = if (currentProduct != null) {
            createUpdateFlowParams(productDetailsParams)
        } else {
            createNewSubscriptionFlowParams(productDetailsParams)
        }

        val billingResultPurchase = flowParams?.let {
            billingClient.launchBillingFlow(activity,
                it
            )
        }
        if (billingResultPurchase != null) {
            if (billingResultPurchase.responseCode != BillingClient.BillingResponseCode.OK) {
                onError(billingResultPurchase.debugMessage)
            }
        }
    }

    private fun createUpdateFlowParams(
        productDetailsParams: List<BillingFlowParams.ProductDetailsParams>
    ): BillingFlowParams? {
        val subscriptionParams = currentProduct?.let {
            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(it.purchaseToken)
                .setSubscriptionReplacementMode(
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
                )
                .build()
        }

        return subscriptionParams?.let {
            partnerUserId?.let { id ->
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParams)
                    .setObfuscatedAccountId(id)
                    .setObfuscatedProfileId(id)
                    .setSubscriptionUpdateParams(it)
                    .build()
            }
        }
    }

    private fun createNewSubscriptionFlowParams(
        productDetailsParams: List<BillingFlowParams.ProductDetailsParams>
    ): BillingFlowParams? {
        return partnerUserId?.let {
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParams)
                .setObfuscatedAccountId(it)
                .setObfuscatedProfileId(it)
                .build()
        }
    }

    override suspend fun checkExistingSubscriptions(
        onError: (String) -> Unit
    ) {
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTED) {
            onError("Billing client not connected")
            return
        }
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val subscription = purchaseList.find { purchase ->
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED ||purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
                        }

                        if (subscription != null) {
                            currentProduct  = subscription
                        }
                    }
                    else -> onError("Failed to query purchases: ${billingResult.debugMessage}")
                }
            }
        } catch (e: Exception) {
            onError("Error checking subscriptions: ${e.message}")
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        purchaseUpdateHandler.handlePurchaseStarted()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val handleRequest = HandlePurchaseRequest(
                productId = purchases[0].products.first().toString(),
                purchaseTime = purchases[0].purchaseTime,
                purchaseToken = purchases[0].purchaseToken,
                partnerUserId = partnerUserId ?: "",

                )
            CoroutineScope(Dispatchers.IO).launch {
                val handlePurchaseResponse = async {
                    productApi.handlePurchase(
                        handleRequest
                    )
                }

                handlePurchaseResponse.await(
                ).let { response ->
                    if (response.isSuccessful) {
                        purchaseUpdateHandler.handlePurchaseUpdate()
                    } else {
                        purchaseUpdateHandler.handlePurchaseFailed()
                    }
                    partnerUserId = null
                }
            }

        } else {
            partnerUserId = null
            purchaseUpdateHandler.handlePurchaseUpdate()
        }
    }
}

