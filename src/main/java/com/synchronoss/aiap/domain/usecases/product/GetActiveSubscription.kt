package com.synchronoss.aiap.domain.usecases.product

import com.synchronoss.aiap.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class GetActiveSubscription @Inject constructor(
    private val repository: ProductManager
) {
    suspend operator fun invoke(userId: String): Resource<ActiveSubscriptionInfo?> {
        return repository.getActiveSubscription(userId = userId)
    }
}