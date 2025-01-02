package com.synchronoss.aiap.data.repository.billing


import android.content.Context
import androidx.activity.ComponentActivity
import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PendingPurchaseUpdate
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.synchronoss.aiap.common.UuidGenerator
import com.synchronoss.aiap.data.remote.HandlePurchaseRequest
import com.synchronoss.aiap.data.remote.ProductApi
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.utils.Constants.PPI_USER_ID
import com.synchronoss.aiap.utils.Constants.PURCHASE
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


class BillingManagerImpl(
    context: Context,
    private val productApi: ProductApi
) : PurchasesUpdatedListener,
    BillingManager {

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    override fun startConnection(onConnected: () -> Unit, onDisconnected: () -> Unit) {
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

    override suspend fun getProducts(
        productIds: List<String>,
        onProductsReceived: (List<ProductDetails>) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails
                }
                onProductsReceived(productDetailsList)
            } else {
                onError(billingResult.debugMessage)
            }
        }
    }

    override suspend fun purchaseSubscription(
        activity: ComponentActivity,
        product: ProductDetails,
        onError: (String) -> Unit
    ) {
        try {
            val offerToken: String = product.subscriptionOfferDetails?.first()?.offerToken ?: ""
            val productDetailsParams = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .setOfferToken(offerToken)
                    .build()
            )
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParams)
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                onError(billingResult.debugMessage)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    private fun calculateExpiryDate(productDetails: ProductDetails, purchaseTime: Long): Long {
        val purchaseDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(purchaseTime), ZoneId.systemDefault())
        val subscriptionOfferDetails = productDetails.subscriptionOfferDetails?.filter { it.offerId ==null }?.firstOrNull()
        val pricingPhaseList = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList
        val billingPeriod = subscriptionOfferDetails?.pricingPhases?.pricingPhaseList?.last()?.billingPeriod

        val expiryDateTime = when {
            billingPeriod?.contains("P1M") == true -> purchaseDateTime.plus(1, ChronoUnit.MONTHS)
            billingPeriod?.contains("P1Y") == true -> purchaseDateTime.plus(1, ChronoUnit.YEARS)
            else -> throw IllegalArgumentException("Unknown subscription type: $billingPeriod")
        }

        return expiryDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val uuid: String = UuidGenerator.generateUUID(purchases[0].packageName)
            val productDetails: ProductDetails? = productDetailsMap[purchases[0].products.first()]

            val modifiedJson : JSONObject = JSONObject (purchases[0].originalJson)
                .put("appId", uuid)
                .put("ppiUserId","5432eb6e-a15c-47c7-94cc-c315551c8413")
                .put("signature",purchases[0].signature)

            println(modifiedJson)


            val handleRequest = HandlePurchaseRequest(
                orderId = purchases[0].orderId.toString(),
                packageName = purchases[0].packageName,
                productId = purchases[0].products.first().toString(),
                purchaseTime = purchases[0].purchaseTime,
                purchaseState = purchases[0].purchaseState,
                purchaseToken = purchases[0].purchaseToken,
                quantity = 1,
                autoRenewing = purchases[0].isAutoRenewing,
                acknowledged = purchases[0].isAcknowledged,
                appId = uuid,
                ppiUserId = PPI_USER_ID,
                signature = purchases[0].signature,
                expiresDate = calculateExpiryDate(
                    productDetails = productDetails!!,
                    purchaseTime = calculateExpiryDate(productDetails, purchases[0].purchaseTime),
                ), // Example expiration date
                transactionId = uuid,
                type = PURCHASE
            )


            GlobalScope.launch {
                productApi.handlePurchase(
                    handleRequest
                )
            }
            //send modified json to backend and get the purchase verified and then acknowledge the purchase

            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchases[0].purchaseToken)
                    .build()
            ) { billingResultAck ->
                if (billingResultAck.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("Purchase acknowledged")
                }
            }

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user canceling the purchase flow.
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ) {
            // Handle an error caused by a user already owning this item
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
            // Handle an error caused by the item being unavailable
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
            // Handle an error caused by the service being disconnected
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR) {
            // Handle an error caused by a timeout
        } else {
            println("Nothing")
            // Handle any other error codes.
        }
    }



}


