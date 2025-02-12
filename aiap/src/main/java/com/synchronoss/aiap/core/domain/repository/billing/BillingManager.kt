package com.synchronoss.aiap.core.domain.repository.billing

import androidx.activity.ComponentActivity
import com.synchronoss.aiap.core.domain.models.ProductInfo


interface BillingManager {
    suspend fun startConnection(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    )

    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productInfo: ProductInfo,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    )
    suspend fun checkExistingSubscriptions(
        onError: (String) -> Unit
    )

}