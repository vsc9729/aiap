package com.synchronoss.aiap.domain.usecases.billing

import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import javax.inject.Inject

class GetProducts(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(
        productIds: List<String>,
        onProductsReceived: (List<ProductDetails>) -> Unit,
        onSubscriptionFound: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        billingManager.getProducts(
            productIds = productIds,
            onProductsReceived = onProductsReceived,
            onSubscriptionFound = onSubscriptionFound,
            onError = onError
        )
    }
}