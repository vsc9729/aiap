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
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import org.json.JSONException
import org.json.JSONObject


class BillingManagerImpl(
    context: Context,
) : PurchasesUpdatedListener,
    BillingManager {
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .setListener(this)
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

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            //To be done from the backend in the future
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
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
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


