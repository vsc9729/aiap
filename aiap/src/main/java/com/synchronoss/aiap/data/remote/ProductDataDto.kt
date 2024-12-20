package com.synchronoss.aiap.data.remote

import com.squareup.moshi.Json

data class ProductDataDto(
    @field:Json(name = "productIds")
    val productData: ProductDto,
)
