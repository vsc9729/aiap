package com.synchronoss.aiap.core.domain.repository.billing

import androidx.activity.ComponentActivity
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.models.ProductInfo


interface BillingManager {
    suspend fun startConnection(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    )

    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productDetails: ProductDetails,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    )
    suspend fun checkExistingSubscriptions(
        onError: (String) -> Unit
    ): ProductDetails?

    suspend fun getProductDetails(
        productIds: List<String>,
        onError: (String) -> Unit
    ): List<ProductDetails>?
}