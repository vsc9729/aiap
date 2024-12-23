package com.synchronoss.aiap.domain.models

data class ProductInfo(
    val productName: String,
    val displayName: String,
    val description: String,
    val ppiId: String,
    val isActive: Boolean,
    val duration: Int
)
