package com.synchronoss.aiap.domain.repository.billing

import androidx.activity.ComponentActivity
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

interface BillingManager {
    fun startConnection(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    )

    suspend fun getProducts(
        productIds: List<String>,
        onProductsReceived: (List<ProductDetails>) -> Unit,
        onSubscriptionFound: (String?) -> Unit,
        onError: (String) -> Unit
    )

    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        product: ProductDetails,
        onError: (String) -> Unit
    )


}