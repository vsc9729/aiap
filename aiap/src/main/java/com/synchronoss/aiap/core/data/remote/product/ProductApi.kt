package com.synchronoss.aiap.core.data.remote.product

import com.synchronoss.aiap.core.data.remote.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for product-related API endpoints.
 * Provides methods for managing subscriptions and products.
 */
interface ProductApi {
    /**
     * Retrieves active subscription information for a user.
     * @param apiKey API key for authentication
     * @param userId Unique identifier of the user
     * @return Response containing active subscription details
     */
    @GET("api/iap/{userid}/Active")
    suspend fun getActiveSubscription(
        @Header("x-api-key") apiKey: String,
        @Path("userid") userId: String
    ): Response<ApiResponse<ActiveSubscriptionResponse>>

    /**
     * Retrieves a list of available products.
     * @param apiKey API key for authentication
     * @return Response containing list of product details
     */
    @GET("api/core/app/product")
    suspend fun getProducts(
        @Header("x-api-key") apiKey: String
    ): ApiResponse<List<ProductDataDto>>

    /**
     * Handles a purchase transaction.
     * @param request Purchase request details
     * @param apiKey API key for authentication
     * @return Response containing purchase handling result
     */
    @POST("api/iap/android/handle")
    suspend fun handlePurchase(
        @Body request: HandlePurchaseRequest,
        @Header("x-api-key") apiKey: String
    ): Response<ApiResponse<HandlePurchaseResponse>>

    /**
     * Handles a purchase transaction with account ID.
     * @param request Purchase request details
     * @param apiKey API key for authentication
     * @return Response containing purchase handling result
     */
    @POST("api/iap/android/handle/accountId")
    suspend fun handlePurchaseAccountId(
        @Body request: HandlePurchaseRequest,
        @Header("x-api-key") apiKey: String
    ): Response<ApiResponse<HandlePurchaseResponse>>
}