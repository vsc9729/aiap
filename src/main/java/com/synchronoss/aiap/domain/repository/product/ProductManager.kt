package com.synchronoss.aiap.domain.repository.product

import com.synchronoss.aiap.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.utils.Resource

interface ProductManager {
    suspend fun getProducts(timestamp: Long?): Resource<List<ProductInfo>>
    suspend fun getActiveSubscription(userId: String): Resource<ActiveSubscriptionInfo?>
}