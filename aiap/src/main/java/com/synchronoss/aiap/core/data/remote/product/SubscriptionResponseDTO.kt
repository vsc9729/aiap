package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object representing subscription information from the remote API.
 *
 * @property product Optional details of the subscribed product
 * @property vendorName Name of the vendor providing the subscription
 * @property appName Name of the application
 * @property appPlatformID Platform-specific application identifier
 * @property platform Platform identifier (e.g., Android, iOS)
 * @property partnerUserId Unique identifier of the subscribed user
 * @property startDate Timestamp when the subscription started
 * @property endDate Timestamp when the subscription ends
 * @property status Current status of the subscription
 * @property type Type of the subscription
 */
@JsonClass(generateAdapter = true)
data class SubscriptionResponseDTO(
    @Json(name = "product")
    val product: ProductDataDto?,
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
    val type: String
)
