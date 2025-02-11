package com.synchronoss.aiap.core.domain.usecases.product

import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class GetActiveSubscription @Inject constructor(
    private val repository: ProductManager
) {
    suspend operator fun invoke(userId: String): Resource<ActiveSubscriptionInfo?> {
        return repository.getActiveSubscription(userId = userId)
    }
}