package com.synchronoss.aiap.data.remote.product

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HandlePurchaseRequest(
    val orderId: String,
    val packageName: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    val purchaseToken: String,
    val quantity: Int,
    val autoRenewing: Boolean,
    val acknowledged: Boolean,
    val appId: String,
    val ppiUserId: String,
    val signature: String,
    val expiresDate: Long,
    val transactionId: String,
    val type: String
)