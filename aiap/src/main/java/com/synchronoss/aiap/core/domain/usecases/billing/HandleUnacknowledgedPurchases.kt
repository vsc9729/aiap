package com.synchronoss.aiap.core.domain.usecases.billing

import com.android.billingclient.api.Purchase
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager

class HandleUnacknowledgedPurchases(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(onError: (String) -> Unit): Boolean {
        return billingManager.handleUnacknowledgedPurchases(onError)
    }
} 