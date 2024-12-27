package com.synchronoss.aiap.data.remote

import com.squareup.moshi.Json

data class ProductDataDto(
    val productName: String,
    @field:Json(name = "displayName")
    val displayName: String,
    @field:Json(name = "description")
    val description: String,
    @field:Json(name = "ppiId")
    val ppiId: String,
    @field:Json(name = "isActive")
    val isActive: Boolean,
    @field:Json(name = "duration")
    val duration: Int
)
