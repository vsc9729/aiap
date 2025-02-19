package com.synchronoss.aiap.core.domain.usecases.billing

/**
 * Collection of use cases for managing billing operations.
 *
 * @property startConnection Use case for establishing connection with billing service
 * @property purchaseSubscription Use case for handling subscription purchases
 * @property checkExistingSubscription Use case for checking existing subscriptions
 * @property getProductDetails Use case for retrieving product details
 */
data class BillingManagerUseCases(
    val startConnection: StartConnection,
    val purchaseSubscription: PurchaseSubscription,
    val checkExistingSubscription: CheckExistingSubscription,
    val getProductDetails: GetProductDetails
)
