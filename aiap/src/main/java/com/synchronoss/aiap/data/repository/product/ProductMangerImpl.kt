package com.synchronoss.aiap.data.repository.product
import ActiveSubscriptionInfo
import android.util.Log
import com.synchronoss.aiap.data.mappers.toActiveSubscriptionInfo
import com.synchronoss.aiap.data.mappers.toProductInfo
import com.synchronoss.aiap.data.remote.ProductApi
import com.synchronoss.aiap.data.remote.ProductDataDto
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

    private var lastKnownProductTimestamp: Long? = null

    override suspend fun getProducts(): Resource<List<ProductInfo>> {
        val activeSubResult = getActiveSubscription()
        if (activeSubResult is Resource.Success) {
            lastKnownProductTimestamp = activeSubResult.data?.productUpdateTimeStamp
        }

        return cacheManager.getCachedDataWithTimestamp(
            key = PRODUCTS_CACHE_KEY,
            currentTimestamp = lastKnownProductTimestamp,
            fetchFromNetwork = {
                try {
                    var productInfos: MutableList<ProductInfo> = mutableListOf()
                    val productDataDtos: List<ProductDataDto> = api.getProducts()
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

    override suspend fun getActiveSubscription(): Resource<ActiveSubscriptionInfo> {
        return try {
            coroutineScope {
                val activeSubDeferred = async { api.getActiveSubscription() }
                val billingCheckDeferred = async {
                    billingManager.checkExistingSubscriptions { error ->
                        Log.d("ProductManagerImpl", "Error: $error")
                    }
                }
                // Wait for both operations to complete
                val response = activeSubDeferred.await()
                billingCheckDeferred.await()

                if (response.isSuccessful) {
                    response.body()?.let { activeSubscriptionResponse ->
                        Resource.Success(activeSubscriptionResponse.toActiveSubscriptionInfo())
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