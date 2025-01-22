package com.synchronoss.aiap.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HandlePurchaseResponse(
    @Json(name = "product")
    val product: ProductDataDto,
    @Json(name = "vendorName")
    val vendorName: String,
    @Json(name = "appName")
    val appName: String,
    @Json(name = "appPlatformID")
    val appPlatformID: String,
    @Json(name = "platform")
    val platform: String,
    @Json(name = "partnerUserId")
    val partnerUserId: String,
    @Json(name = "startDate")
    val startDate: Long,
    @Json(name = "endDate")
    val endDate: Long,
    @Json(name = "status")
    val status: String,
    @Json(name = "type")
    val type: String,
)
