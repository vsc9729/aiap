package com.synchronoss.aiap.core.domain.usecases.billing

import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import kotlinx.coroutines.CompletableDeferred

class StartConnection(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(): CompletableDeferred<Unit> {
        return billingManager.startConnection()
    }
}