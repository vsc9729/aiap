package com.synchronoss.aiap.domain.repository.product

import ActiveSubscriptionInfo
import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.utils.Resource

interface ProductManager {
    suspend fun getProducts(timestamp: Long?): Resource<List<ProductInfo>>
    suspend fun getActiveSubscription(): Resource<ActiveSubscriptionInfo?>
}