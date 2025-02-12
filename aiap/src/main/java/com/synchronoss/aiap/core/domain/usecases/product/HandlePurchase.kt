package com.synchronoss.aiap.core.domain.usecases.product

import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import javax.inject.Inject

class HandlePurchase @Inject constructor(
    private val repository: ProductManager
) {
    suspend operator fun invoke(request: HandlePurchaseRequest, apiKey: String): Boolean {
        return repository.handlePurchase(request, apiKey)
    }
} 