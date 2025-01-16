package com.synchronoss.aiap.data.repository.billing

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
import com.synchronoss.aiap.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.di.PurchaseUpdateHandler
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.utils.Constants.PPI_USER_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class BillingManagerImpl(
    context: Context,
    private val productApi: ProductApi,
    private val purchaseUpdateHandler: PurchaseUpdateHandler
) : PurchasesUpdatedListener,
    BillingManager {

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private var currentProduct :Purchase? = null
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override suspend fun startConnection(onConnected: () -> Unit, onDisconnected: () -> Unit) {
        coroutineScope {
         val connect =async {
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
            connect.await()
        }

    }


    override suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productInfo: ProductInfo,
        onError: (String) -> Unit,
    ) {
        try {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productInfo.productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build())
                ).build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    var product:ProductDetails? = productDetailsList[0]
                    productDetailsList.forEach { productDetails ->
                        productDetailsMap[productDetails.productId] = productDetails
                        val offerToken: String = product!!.subscriptionOfferDetails?.first()?.offerToken ?: ""
                        val productDetailsParams = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product)
                                .setOfferToken(offerToken)
                                .build()
                        )

                        if(currentProduct != null){
                            val subscriptionParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                .setOldPurchaseToken(currentProduct!!.purchaseToken)
                                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
                                .build()

                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParams)
                                .setObfuscatedAccountId(PPI_USER_ID)
                                .setObfuscatedProfileId(PPI_USER_ID)
                                .setSubscriptionUpdateParams(subscriptionParams)
                                .build()

                            val billingResultPurchase = billingClient.launchBillingFlow(activity, flowParams)
                            if (billingResultPurchase.responseCode != BillingClient.BillingResponseCode.OK) {
                                onError(billingResultPurchase.debugMessage)
                            }
                        } else {
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParams)

                                .build()
                            val billingResultPurchase = billingClient.launchBillingFlow(activity, flowParams)
                            if (billingResultPurchase.responseCode != BillingClient.BillingResponseCode.OK) {
                                onError(billingResultPurchase.debugMessage)
                            }
                        }
                    }
                }
            }



        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        purchaseUpdateHandler.handlePurchaseStarted()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val handleRequest = HandlePurchaseRequest(
                productId = purchases[0].products.first().toString(),
                purchaseTime = purchases[0].purchaseTime,
                purchaseToken = purchases[0].purchaseToken,
                partnerUserId = PPI_USER_ID,

            )
            CoroutineScope(Dispatchers.IO).launch {
                val handlePurchaseResponse = async {
                    productApi.handlePurchase(
                        handleRequest
                    )
                }
                handlePurchaseResponse.await(
                ).let { _ ->
                    purchaseUpdateHandler.handlePurchaseUpdate()
                }
            }

        } else {
         //   when (billingResult.responseCode) {
//                BillingClient.BillingResponseCode.USER_CANCELED -> {
//                    purchaseUpdateHandler.handlePurchaseUpdate()
//                    // Handle an error caused by a user canceling the purchase flow.
//                }
//                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                    purchaseUpdateHandler.handlePurchaseUpdate()
//                    // Handle an error caused by a user already owning this item
//                }
//                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
//                    purchaseUpdateHandler.handlePurchaseUpdate()
//                    // Handle an error caused by the item being unavailable
//                }
//                else -> {
//                    purchaseUpdateHandler.handlePurchaseUpdate()
//                }
                purchaseUpdateHandler.handlePurchaseUpdate()
            }
        }
    }

