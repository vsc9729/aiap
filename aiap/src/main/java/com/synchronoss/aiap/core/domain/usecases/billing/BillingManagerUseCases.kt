package com.synchronoss.aiap.core.domain.usecases.billing

data class BillingManagerUseCases(
    val startConnection: StartConnection,
    val purchaseSubscription: PurchaseSubscription,
    val checkExistingSubscription: CheckExistingSubscription,
    val getProductDetails: GetProductDetails
)
