package com.synchronoss.aiap.data.remote.product

import com.synchronoss.aiap.data.remote.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("api/iap/{userid}/Active")
    suspend fun getActiveSubscription(
        @Header("x-api-key") apiKey: String = "IAPAppAndroid",
        @Path("userid") userId: String
    ): Response<ApiResponse<ActiveSubscriptionResponse>>
    @GET("api/core/app/product")
    suspend fun getProducts(
        @Header("x-api-key") apiKey: String = "IAPAppAndroid"
    ): ApiResponse<List<ProductDataDto>>
    @POST("api/iap/android/handle")
    suspend fun handlePurchase(
        @Body request: HandlePurchaseRequest,
        @Header("x-api-key") apiKey: String? = "IAPAppAndroid"
    ): Response<ApiResponse<HandlePurchaseResponse>>
}