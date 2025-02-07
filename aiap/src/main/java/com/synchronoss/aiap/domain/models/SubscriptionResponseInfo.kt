package com.synchronoss.aiap.domain.models

import com.synchronoss.aiap.domain.models.ProductInfo

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