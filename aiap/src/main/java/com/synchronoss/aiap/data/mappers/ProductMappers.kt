package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.data.remote.ProductDataDto
import com.synchronoss.aiap.data.remote.ProductDto
import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.models.ProductInfo

fun ProductDto.toProductData(): ProductData {
    return ProductData(
        products = products.map { ProductInfo(it) }
    )
}


