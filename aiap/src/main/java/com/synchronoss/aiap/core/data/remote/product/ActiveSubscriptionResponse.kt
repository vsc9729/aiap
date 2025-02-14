package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActiveSubscriptionResponse(
    @Json(name = "subscriptionResponseDTO")
    val subscriptionResponseDTO: SubscriptionResponseDTO?,

    @Json(name = "productUpdateTimeStamp")
    val productUpdateTimeStamp: Long?,

    @Json(name = "themConfigTimeStamp")
    val themConfigTimeStamp: Long?,

    @Json(name = "userUUID")
    val userUUID: String
)