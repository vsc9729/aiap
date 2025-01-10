package com.synchronoss.aiap.data.remote

import com.squareup.moshi.Json

data class ProductDataDto(
    @field:Json(name = "productId")
    val productId: String,
    @field:Json(name = "displayName")
    val displayName: String,
    @field:Json(name = "description")
    val description: String,
    @field:Json(name = "vendorName")
    val vendorName: String,
    @field:Json(name = "appName")
    val appName: String,
    @field:Json(name = "price")
    val price: Double,
    @field:Json(name = "displayPrice")
    val displayPrice: String,
    @field:Json(name = "platform")
    val platform: String,
    @field:Json(name = "serviceLevel")
    val serviceLevel: String,
    @field:Json(name = "isActive")
    val isActive: Boolean,
    @field:Json(name = "recurringPeriodCode")
    val recurringPeriodCode: String,
    @field:Json(name = "productType")
    val productType: String,
    @field:Json(name = "entitlementId")
    val entitlementId: String?
)
