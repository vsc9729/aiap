package com.synchronoss.aiap.core.domain.usecases.product

import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import javax.inject.Inject

class HandlePurchase @Inject constructor(
    private val repository: ProductManager
) {
    suspend operator fun invoke(
        productId: String,
        purchaseTime: Long,
        purchaseToken: String,
        partnerUserId: String,
        apiKey: String
    ): Boolean {
        val request = HandlePurchaseRequest(
            productId = productId,
            purchaseTime = purchaseTime,
            purchaseToken = purchaseToken,
            partnerUserId = partnerUserId
        )
        return repository.handlePurchase(request, apiKey)
    }
} 