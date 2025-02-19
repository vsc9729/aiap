package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object representing a purchase handling request to the remote API.
 *
 * @property productId Identifier of the product being purchased
 * @property purchaseTime Timestamp when the purchase was made
 * @property purchaseToken Token received from the billing service for purchase verification
 * @property partnerUserId Unique identifier of the user making the purchase
 */
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