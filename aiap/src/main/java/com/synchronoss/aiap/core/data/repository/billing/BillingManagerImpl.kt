package com.synchronoss.aiap.core.data.repository.billing

import android.content.Context
import androidx.activity.ComponentActivity
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
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred

/**
 * Implementation of BillingManager interface that handles Google Play Billing operations.
 * Manages subscription purchases, billing client connection, and purchase updates.
 *
 * @property context Android application context
 * @property productManagerUseCases Use cases for product management operations
 * @property purchaseUpdateHandler Handler for purchase-related events
 */
class BillingManagerImpl(
    context: Context,
    private val productManagerUseCases: ProductManagerUseCases,
    private val purchaseUpdateHandler: PurchaseUpdateHandler
) : PurchasesUpdatedListener,
    BillingManager {

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private var currentProduct: Purchase? = null
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    /**
     * Establishes connection with the billing service.
     * @return CompletableDeferred that completes when connection is established
     */
    override suspend fun startConnection(): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    deferred.complete(Unit)
                } else {
                    deferred.completeExceptionally(Exception(billingResult.debugMessage))
                }
            }

            override fun onBillingServiceDisconnected() {
                deferred.completeExceptionally(Exception("Billing service disconnected"))
            }
        })
        
        return deferred
    }

    /**
     * Initiates a subscription purchase flow.
     * @param activity Activity from which to launch the purchase flow
     * @param productDetails Details of the product to purchase
     * @param onError Callback for error handling
     * @param userId User identifier for the purchase
     * @param apiKey API key for authentication
     */
    override suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productDetails: ProductDetails,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    ) {
//        GlobalBillingConfig.partnerUserId = userId
//        GlobalBillingConfig.apiKey = apiKey

        try {
            handleProductDetails(activity, listOf(productDetails), onError)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    /**
     * Handles product details for purchase flow.
     * @param activity Activity context
     * @param productDetailsList List of product details
     * @param onError Error callback
     */
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

    /**
     * Creates product details parameters for billing flow.
     * @param product Product details
     * @return List of product details parameters
     */
    private fun createProductDetailsParams(product: ProductDetails): List<BillingFlowParams.ProductDetailsParams> {
        val offerToken: String = when {
            product.subscriptionOfferDetails!!.size > 1 ->  product.subscriptionOfferDetails!![product.subscriptionOfferDetails!!.size-2].offerToken
            else -> product.subscriptionOfferDetails!!.first().offerToken
        }
        return listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()
        )
    }

    /**
     * Launches the billing flow for purchase.
     * @param activity Activity context
     * @param productDetailsParams Product details parameters
     * @param onError Error callback
     */
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
            GlobalBillingConfig.userUUID?.let { id ->
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
        return GlobalBillingConfig.userUUID.let {
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParams)
                .setObfuscatedAccountId(it)
                .setObfuscatedProfileId(it)
                .build()
        }
    }

    /**
     * Checks for existing subscriptions.
     * @param onError Error callback
     * @return ProductDetails of existing subscription or null if none exists
     */
    override suspend fun checkExistingSubscriptions(
        onError: (String) -> Unit
    ): ProductDetails? = coroutineScope {
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTED) {
            onError("Billing client not connected")
            return@coroutineScope null
        }
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val deferred = CompletableDeferred<ProductDetails?>()

            billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val subscription = purchaseList.find { purchase ->
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED || purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE
                        }

                        if (subscription != null) {
                            currentProduct = subscription
                            // Get product details for the current subscription
                            val productId = subscription.products.firstOrNull()
                            if (productId != null) {
                                val productList = listOf(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build()
                                )

                                val productParams = QueryProductDetailsParams.newBuilder()
                                    .setProductList(productList)
                                    .build()

                                billingClient.queryProductDetailsAsync(productParams) { detailsResult, productDetailsList ->
                                    if (detailsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                        deferred.complete(productDetailsList.firstOrNull())
                                    } else {
                                        onError(detailsResult.debugMessage)
                                        deferred.complete(null)
                                    }
                                }
                            } else {
                                deferred.complete(null)
                            }
                        } else {
                            deferred.complete(null)
                        }
                    }
                    else -> {
                        onError("Failed to query purchases: ${billingResult.debugMessage}")
                        deferred.complete(null)
                    }
                }
            }
            deferred.await()
        } catch (e: Exception) {
            onError("Error checking subscriptions: ${e.message}")
            null
        }
    }

    /**
     * Callback for purchase updates from the billing client.
     * @param billingResult Result of the purchase operation
     * @param purchases List of purchases
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        purchaseUpdateHandler.handlePurchaseStarted()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val productId = purchases[0].products.first().toString()
            val purchaseTime = purchases[0].purchaseTime
            val purchaseToken = purchases[0].purchaseToken
            val partnerUserId = GlobalBillingConfig.userUUID
            
            CoroutineScope(Dispatchers.IO).launch {
                GlobalBillingConfig.apiKey.let { key ->
                    val handlePurchaseResponse = async {
                        productManagerUseCases.handlePurchase(
                            productId = productId,
                            purchaseTime = purchaseTime,
                            purchaseToken = purchaseToken,
                            partnerUserId = partnerUserId,
                            apiKey = key
                        )
                    }

                    val success = handlePurchaseResponse.await()
                    if (success) {
                        purchaseUpdateHandler.handlePurchaseUpdate()
                    } else {
                        purchaseUpdateHandler.handlePurchaseFailed()
                    }
                }
            }
        } else {
            purchaseUpdateHandler.handlePurchaseStopped()
        }
    }

    /**
     * Retrieves details of products from the billing client.
     * @param productIds List of product identifiers
     * @param onError Error callback
     * @return List of product details or null if retrieval fails
     */
    override suspend fun getProductDetails(
        productIds: List<String>,
        onError: (String) -> Unit
    ): List<ProductDetails>? = coroutineScope {
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTED) {
            onError("Billing client not connected")
            return@coroutineScope null
        }

        try {
            val productList = productIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val deferred = CompletableDeferred<List<ProductDetails>?>()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    deferred.complete(productDetailsList)
                } else {
                    onError(billingResult.debugMessage)
                    deferred.complete(null)
                }
            }

            deferred.await()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error fetching product details")
            null
        }
    }

    override suspend fun handleUnacknowledgedPurchases(onError: (String) -> Unit): Boolean = coroutineScope {
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTED) {
            onError("Billing client not connected")
            return@coroutineScope false
        }

        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val deferred = CompletableDeferred<Boolean>()

            billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val unacknowledgedPurchase = purchaseList.find { purchase ->
                            !purchase.isAcknowledged && 
                            (purchase.purchaseState == Purchase.PurchaseState.PURCHASED || 
                             purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE)
                        }

                        if (unacknowledgedPurchase != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val productId = unacknowledgedPurchase.products[0]
                                val purchaseTime = unacknowledgedPurchase.purchaseTime
                                val purchaseToken = unacknowledgedPurchase.purchaseToken
                                val partnerUserId = GlobalBillingConfig.userUUID

                                GlobalBillingConfig.apiKey.let { key ->
                                    val handlePurchaseResponse = async {
                                        productManagerUseCases.handlePurchase(
                                            productId = productId,
                                            purchaseTime = purchaseTime,
                                            purchaseToken = purchaseToken,
                                            partnerUserId = partnerUserId,
                                            apiKey = key
                                        )
                                    }

                                    val success = handlePurchaseResponse.await()
                                    if (success) {
                                        purchaseUpdateHandler.handlePurchaseUpdate()
                                        deferred.complete(true)
                                    } else {
                                        deferred.complete(false)
                                    }
                                }
                            }
                        } else {
                            deferred.complete(false)
                        }
                    }
                    else -> {
                        onError("Failed to query purchases: ${billingResult.debugMessage}")
                        deferred.complete(false)
                    }
                }
            }
            deferred.await()
        } catch (e: Exception) {
            onError("Error handling unacknowledged purchases: ${e.message}")
            false
        }
    }
}

