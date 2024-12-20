package com.synchronoss.aiap.data.remote

import retrofit2.http.GET

interface ProductApi {
    @GET("/v3/qs/676520c4ad19ca34f8de2e89")//Dummy endpoint from JsonBin
    suspend fun getProducts(): ProductDto
}