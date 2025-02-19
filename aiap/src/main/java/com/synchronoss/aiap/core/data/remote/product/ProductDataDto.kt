package com.synchronoss.aiap.core.data.remote.product

import com.squareup.moshi.Json

/**
 * Data Transfer Object representing product information from the remote API.
 *
 * @property id Unique identifier for the product
 * @property productId Product identifier used for billing
 * @property displayName Optional display name of the product
 * @property description Optional description of the product
 * @property vendorName Name of the vendor providing the product
 * @property appName Name of the application
 * @property price Price of the product
 * @property displayPrice Optional formatted price string for display
 * @property platform Platform identifier (e.g., Android, iOS)
 * @property serviceLevel Service level of the product
 * @property isActive Whether the product is currently active
 * @property recurringPeriodCode Optional code indicating subscription period
 * @property productType Type of the product
 * @property entitlementId Optional identifier for product entitlement
 */
data class ProductDataDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "productId")
    val productId: String,
    @field:Json(name = "displayName")
    val displayName: String?,
    @field:Json(name = "description")
    val description: String?,
    @field:Json(name = "vendorName")
    val vendorName: String,
    @field:Json(name = "appName")
    val appName: String,
    @field:Json(name = "price")
    val price: Double,
    @field:Json(name = "displayPrice")
    val displayPrice: String?,
    @field:Json(name = "platform")
    val platform: String,
    @field:Json(name = "serviceLevel")
    val serviceLevel: String,
    @field:Json(name = "isActive")
    val isActive: Boolean,
    @field:Json(name = "recurringPeriodCode")
    val recurringPeriodCode: String?,
    @field:Json(name = "productType")
    val productType: String,
    @field:Json(name = "entitlementId")
    val entitlementId: String?
)
