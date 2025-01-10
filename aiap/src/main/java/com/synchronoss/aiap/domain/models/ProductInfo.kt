package com.synchronoss.aiap.domain.models

data class ProductInfo(
    val productId: String,
    val displayName: String,
    val description: String,
    val vendorName: String,
    val appName: String,
    val price: Double,
    val displayPrice: String,
    val platform: String,
    val serviceLevel: String,
    val isActive: Boolean,
    val recurringPeriodCode: String,
    val productType: String,
    val entitlementId: String?
)
