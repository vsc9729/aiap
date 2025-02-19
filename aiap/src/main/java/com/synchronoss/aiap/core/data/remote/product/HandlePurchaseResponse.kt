package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object representing a purchase handling response from the remote API.
 *
 * @property product Details of the purchased product
 * @property vendorName Name of the vendor providing the product
 * @property appName Name of the application
 * @property appPlatformID Platform-specific application identifier
 * @property platform Platform identifier (e.g., Android, iOS)
 * @property partnerUserId Unique identifier of the user who made the purchase
 * @property startDate Timestamp when the subscription starts
 * @property endDate Timestamp when the subscription ends
 * @property status Current status of the purchase
 * @property type Type of the purchase
 */
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
