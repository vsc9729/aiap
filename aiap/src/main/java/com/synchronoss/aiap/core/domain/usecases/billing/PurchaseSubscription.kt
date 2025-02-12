package com.synchronoss.aiap.core.domain.usecases.billing

import androidx.activity.ComponentActivity
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager

class PurchaseSubscription(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(
        activity: ComponentActivity,
        product: ProductInfo,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    ) {
        billingManager.purchaseSubscription(
            activity = activity,
            productInfo = product,
            onError = onError,
            userId = userId,
            apiKey = apiKey
        )
    }
}