package com.synchronoss.aiap.domain.repository.product

import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.utils.Resource

interface ProductManager {
    suspend fun  getProducts(): Resource<List<ProductInfo>>
}