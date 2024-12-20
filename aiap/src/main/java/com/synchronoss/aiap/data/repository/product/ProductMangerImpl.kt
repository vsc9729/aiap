package com.synchronoss.aiap.data.repository.product

import com.synchronoss.aiap.data.mappers.toProductData
import com.synchronoss.aiap.data.remote.ProductApi
import com.synchronoss.aiap.domain.models.ProductData
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class ProductMangerImpl @Inject constructor(
    private  val api: ProductApi
) :ProductManager{
    override suspend fun getProducts() : Resource<ProductData>{
        return try {
            Resource.Success(api.getProducts().toProductData())
        }catch (e: Exception){
            Resource.Error(e.message ?: "Failed to fetch data. Unknown error")
        }
    }
}