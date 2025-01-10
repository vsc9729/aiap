package com.synchronoss.aiap.data.repository.product


import com.synchronoss.aiap.data.mappers.toProductInfo
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.data.remote.product.ProductDataDto
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class ProductMangerImpl @Inject constructor(
    private val api: ProductApi
) : ProductManager {
    override suspend fun getProducts(): Resource<List<ProductInfo>> {
        return try {
            val productInfos: MutableList<ProductInfo> = mutableListOf()
            val productDataDtos: List<ProductDataDto> = api.getProducts()
            for (productDataDto in productDataDtos) {
                val productInfo: ProductInfo = productDataDto.toProductInfo()
                productInfos.add(productInfo)
            }
            Resource.Success(productInfos)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch data. Unknown error")
        }
    }
}