package com.synchronoss.aiap.data.mappers

import com.synchronoss.aiap.data.remote.ProductDataDto
import com.synchronoss.aiap.data.remote.ProductDto
import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.models.ProductInfo

fun ProductDataDto.toProductInfo(): ProductInfo {
    return ProductInfo(
        productName = productName,
        displayName = displayName,
        description = description,
        ppiId = ppiId,
        isActive = isActive,
        duration = duration
    )
}


