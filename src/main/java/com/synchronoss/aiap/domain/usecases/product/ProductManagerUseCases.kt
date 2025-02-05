package com.synchronoss.aiap.domain.usecases.product


data class ProductManagerUseCases(
    val getProductsApi: GetProductsApi,
    val getActiveSubscription: GetActiveSubscription
)