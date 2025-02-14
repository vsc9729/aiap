package com.synchronoss.aiap.core.domain.usecases.billing

import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager

class GetProductDetails(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(
        productIds: List<String>,
        onError: (String) -> Unit
    ): List<ProductDetails>? {
        return billingManager.getProductDetails(productIds, onError)
    }
} 