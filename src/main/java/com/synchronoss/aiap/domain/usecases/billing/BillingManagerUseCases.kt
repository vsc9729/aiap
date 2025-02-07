package com.synchronoss.aiap.domain.usecases.billing

data class BillingManagerUseCases(
    val startConnection: StartConnection,
    val purchaseSubscription: PurchaseSubscription,
    val checkExistingSubscription: CheckExistingSubscription
)
