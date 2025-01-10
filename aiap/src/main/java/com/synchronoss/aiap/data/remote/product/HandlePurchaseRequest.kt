package com.synchronoss.aiap.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HandlePurchaseRequest(
    @Json(name = "productId")
    val productId: String,
    @Json(name = "purchaseTime")
    val purchaseTime: Long,
    @Json(name = "purchaseToken")
    val purchaseToken: String,
    @Json(name = "partnerUserId")
    val partnerUserId: String
)