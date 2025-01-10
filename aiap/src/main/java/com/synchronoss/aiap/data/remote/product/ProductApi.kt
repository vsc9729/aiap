package com.synchronoss.aiap.data.remote.product

import com.synchronoss.aiap.utils.Constants.PPI_USER_ID
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductApi {
    @GET("api/iap/$PPI_USER_ID/Active")
    suspend fun getActiveSubscription(
        @Header("x-api-key") apiKey: String = "IAPAppAndroid"
    ): Response<ActiveSubscriptionResponse>
    @GET("api/core/product")
    suspend fun getProducts(
        @Header("x-api-key") apiKey: String = "IAPAppAndroid"
    ): List<ProductDataDto>
    @POST("api/iap/android/handle")
    suspend fun handlePurchase(
        @Body request: HandlePurchaseRequest,
        @Header("x-api-key") apiKey: String? = "IAPAppAndroid"
    ): Response<HandlePurchaseResponse>
}