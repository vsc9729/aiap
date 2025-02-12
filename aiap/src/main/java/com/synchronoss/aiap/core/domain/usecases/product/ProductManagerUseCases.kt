package com.synchronoss.aiap.core.domain.usecases.product


data class ProductManagerUseCases(
    val getProductsApi: GetProductsApi,
    val getActiveSubscription: GetActiveSubscription,
    val handlePurchase: HandlePurchase
)