package com.synchronoss.aiap.core.domain.models


data class SubscriptionResponseInfo(
    val product: ProductInfo?,
    val vendorName: String,
    val appName: String,
    val appPlatformID: String,
    val platform: String,
    val partnerUserId: String,
    val startDate: Long,
    val endDate: Long,
    val status: String,
    val type: String
)