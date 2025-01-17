package com.synchronoss.aiap.domain.usecases.billing

import androidx.activity.ComponentActivity
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.billing.BillingManager

class PurchaseSubscription(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(
        activity: ComponentActivity,
        product: ProductInfo,
        onError: (String) -> Unit
    ) {
        billingManager.purchaseSubscription(
            activity = activity,
            productInfo = product,
            onError = onError
        )
    }
}