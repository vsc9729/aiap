package com.synchronoss.aiap.data.remote.product

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductApi {
    @GET("api/core/product")
    suspend fun getProducts(): List<ProductDataDto>
    @POST("api/iap/android/handle")
    suspend fun handlePurchase(
        @Body request: HandlePurchaseRequest
    ): Response<Unit>
}