package com.synchronoss.aiap.data.remote

import retrofit2.http.GET

interface ProductApi {
    @GET("api/core/product")
    suspend fun getProducts(): List<ProductDataDto>
}