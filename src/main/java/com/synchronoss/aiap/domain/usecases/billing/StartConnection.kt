package com.synchronoss.aiap.domain.usecases.billing

import com.synchronoss.aiap.domain.repository.billing.BillingManager

class StartConnection(
    private val billingManager: BillingManager
) {
    suspend operator fun invoke(onConnected: () -> Unit, onDisconnected: () -> Unit) {
        billingManager.startConnection(onConnected = onConnected, onDisconnected = onDisconnected)
    }

}