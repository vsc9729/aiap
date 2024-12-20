package com.synchronoss.aiap.data.remote

import com.squareup.moshi.Json

data class ProductDto(
    val products: List<String>,
    val productCount: Int
)
