package com.synchronoss.aiap.core.domain.repository.product

import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.utils.Resource

interface ProductManager {
    suspend fun getProducts(timestamp: Long?, apiKey: String): Resource<List<ProductInfo>>
    suspend fun getActiveSubscription(userId: String, apiKey: String): Resource<ActiveSubscriptionInfo?>
    suspend fun handlePurchase(request: HandlePurchaseRequest, apiKey: String): Boolean
}