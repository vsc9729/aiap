package com.synchronoss.aiap.data.repository.product
import ActiveSubscriptionInfo
import android.util.Log
import com.synchronoss.aiap.data.mappers.toActiveSubscriptionInfo
import com.synchronoss.aiap.data.mappers.toProductInfo
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.data.remote.product.ProductDataDto
import com.synchronoss.aiap.domain.models.ProductInfo
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import com.synchronoss.aiap.data.remote.common.ApiResponse
import javax.inject.Inject

class ProductMangerImpl @Inject constructor(
    private val api: ProductApi,
    private val billingManager: BillingManager,
    private val cacheManager: CacheManager
) : ProductManager {

    companion object {
        private const val PRODUCTS_CACHE_KEY = "products_cache"
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(
        List::class.java, 
        ProductInfo::class.java
    )
    private val jsonAdapter = moshi.adapter<List<ProductInfo>>(listType).lenient()


    override suspend fun getProducts(timestamp: Long?): Resource<List<ProductInfo>> {


        return cacheManager.getCachedDataWithTimestamp(
            key = PRODUCTS_CACHE_KEY,
            currentTimestamp = timestamp,
            fetchFromNetwork = {
                try {
                    var productInfos: MutableList<ProductInfo> = mutableListOf()
                    val apiResponse: ApiResponse<List<ProductDataDto>> = api.getProducts()
                    val productDataDtos: List<ProductDataDto> = apiResponse.data
                    for (productDataDto in productDataDtos) {
                        val productInfo: ProductInfo = productDataDto.toProductInfo()
                        productInfos.add(productInfo)
                    }
                    productInfos = productInfos.filter { it.platform == "ANDROID" } as MutableList<ProductInfo>
                    Resource.Success(productInfos)
                } catch (e: Exception) {
                    Resource.Error(e.message ?: "Failed to fetch data. Unknown error")
                }
            },
            serialize = { products ->
                jsonAdapter.toJson(products)
            },
            deserialize = { jsonString ->
                jsonAdapter.fromJson(jsonString) ?: emptyList()
            }
        )
    }

    override suspend fun getActiveSubscription(): Resource<ActiveSubscriptionInfo?> {
        return try {
            coroutineScope {
                val activeSubDeferred = async { api.getActiveSubscription() }
                // Wait for both operations to complete
                val response = activeSubDeferred.await()
                if (response.isSuccessful) {
                    response.body()?.let { activeSubscriptionResponse ->
                        Resource.Success(activeSubscriptionResponse.data.toActiveSubscriptionInfo())
                    } ?: Resource.Error("Empty response body")
                } else {
                    Resource.Error("Failed to fetch active subscription: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch active subscription. Unknown error")
        }
    }
}