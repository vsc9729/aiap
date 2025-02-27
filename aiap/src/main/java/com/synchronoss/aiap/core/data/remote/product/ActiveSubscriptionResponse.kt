package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object representing an active subscription response from the remote API.
 *
 * @property subscriptionResponseDTO Detailed subscription information
 * @property productUpdateTimeStamp Timestamp of the last product update
 * @property themConfigTimeStamp Timestamp of the last theme configuration update
 * @property userUUID Unique identifier of the user
 */
@JsonClass(generateAdapter = true)
data class ActiveSubscriptionResponse(
    @Json(name = "subscriptionResponseDTO")
    val subscriptionResponseDTO: SubscriptionResponseDTO?,

    @Json(name = "productUpdateTimeStamp")
    val productUpdateTimeStamp: Long?,

    @Json(name = "themConfigTimeStamp")
    val themConfigTimeStamp: Long?,

    @Json(name = "userUUID")
    val userUUID: String,

    @Json(name = "baseServiceLevel")
    val baseServiceLevel: String,
)