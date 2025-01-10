package com.synchronoss.aiap.domain.usecases.product

import GetActiveSubscription

data class ProductManagerUseCases(
    val getProductsApi: GetProductsApi,
    val getActiveSubscription: GetActiveSubscription
)