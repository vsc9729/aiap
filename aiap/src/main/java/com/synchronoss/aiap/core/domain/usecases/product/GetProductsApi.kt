package com.synchronoss.aiap.core.domain.usecases.product

import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource

class GetProductsApi(
    private val productManager: ProductManager
) {
    suspend operator fun invoke(timestamp: Long?): Resource<List<ProductInfo>> {
        return productManager.getProducts(timestamp)
    }
}