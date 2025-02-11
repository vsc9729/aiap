package com.synchronoss.aiap.core.data.repository.product
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.core.data.mappers.toActiveSubscriptionInfo
import com.synchronoss.aiap.core.data.mappers.toProductInfo
import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.remote.product.ProductDataDto
import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.repository.product.ProductManager
import com.synchronoss.aiap.utils.CacheManager
import com.synchronoss.aiap.utils.Resource
import javax.inject.Inject

class ProductManagerImpl @Inject constructor(
    private val api: ProductApi,
    private val billingManager: BillingManager,
    private val cacheManager: CacheManager
) : ProductManager {

    companion object {
        private const val PRODUCTS_CACHE_KEY = "products_cache"
        private const val ACTIVE_SUBSCRIPTION_CACHE_KEY = "active_subscription_cache"
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(
        List::class.java, 
        ProductInfo::class.java
    )
    private val jsonAdapter = moshi.adapter<List<ProductInfo>>(listType).lenient()
    private val subscriptionAdapter = moshi.adapter(ActiveSubscriptionInfo::class.java).lenient()

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

    override suspend fun getActiveSubscription(userId: String): Resource<ActiveSubscriptionInfo?> {
        return cacheManager.getCachedDataWithNetwork(
            key = "${ACTIVE_SUBSCRIPTION_CACHE_KEY}_${userId}",
            fetchFromNetwork = {
                try {
                
                        val response = api.getActiveSubscription(userId = userId)
                        if (response.isSuccessful) {
                            response.body()?.let { activeSubscriptionResponse ->
                                Resource.Success(activeSubscriptionResponse.data.toActiveSubscriptionInfo())
                            } ?: Resource.Error("Empty response body 1")
                        } else {
                            Resource.Error("Failed to fetch active subscription: ${response.message()} 2")
                        }
                    
                } catch (e: Exception) {
                    Resource.Error(e.message ?: "Failed to fetch active subscription. Unknown error 3")
                }
            },
            serialize = { subscription ->
                subscriptionAdapter.toJson(subscription)
            },
            deserialize = { jsonString ->
                subscriptionAdapter.fromJson(jsonString)
            }
        )
    }
}