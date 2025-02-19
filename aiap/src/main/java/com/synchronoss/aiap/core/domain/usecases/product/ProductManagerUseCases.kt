package com.synchronoss.aiap.core.domain.usecases.product

/**
 * Collection of use cases for managing product operations.
 *
 * @property getProductsApi Use case for retrieving products from the API
 * @property getActiveSubscription Use case for retrieving active subscription information
 * @property handlePurchase Use case for processing purchase transactions
 */
data class ProductManagerUseCases(
    val getProductsApi: GetProductsApi,
    val getActiveSubscription: GetActiveSubscription,
    val handlePurchase: HandlePurchase
)