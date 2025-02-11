package com.synchronoss.aiap.core.domain.usecases.billing

import com.synchronoss.aiap.core.domain.repository.billing.BillingManager


class CheckExistingSubscription(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(onError: (String) -> Unit) {
        billingManager.checkExistingSubscriptions(onError = onError)
    }

}