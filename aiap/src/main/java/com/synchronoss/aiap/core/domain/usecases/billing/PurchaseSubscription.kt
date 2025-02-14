package com.synchronoss.aiap.core.domain.usecases.billing

import androidx.activity.ComponentActivity
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager

class PurchaseSubscription(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(
        activity: ComponentActivity,
        product: ProductDetails,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    ) {
        billingManager.purchaseSubscription(
            activity = activity,
            productDetails = product,
            onError = onError,
            userId = userId,
            apiKey = apiKey
        )
    }
}