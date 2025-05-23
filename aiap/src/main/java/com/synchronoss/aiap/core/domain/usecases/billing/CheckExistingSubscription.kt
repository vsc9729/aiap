package com.synchronoss.aiap.core.domain.usecases.billing

import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager


class CheckExistingSubscription(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(onError: (String) -> Unit): ProductDetails? {
        return billingManager.checkExistingSubscriptions(onError = onError)
    }

}