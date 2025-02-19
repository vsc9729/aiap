package com.synchronoss.aiap.core.domain.repository.product

import com.synchronoss.aiap.core.data.remote.product.HandlePurchaseRequest
import com.synchronoss.aiap.core.domain.models.ActiveSubscriptionInfo
import com.synchronoss.aiap.core.domain.models.ProductInfo
import com.synchronoss.aiap.utils.Resource

/**
 * Interface for managing product-related operations.
 * Handles product information retrieval, active subscription status, and purchase processing.
 */
interface ProductManager {
    /**
     * Retrieves a list of available products.
     * @param timestamp Optional timestamp for cache validation
     * @param apiKey API key for authentication
     * @return Resource containing a list of ProductInfo objects
     */
    suspend fun getProducts(timestamp: Long?, apiKey: String): Resource<List<ProductInfo>>

    /**
     * Retrieves information about the user's active subscription.
     * @param userId The unique identifier of the user
     * @param apiKey API key for authentication
     * @return Resource containing ActiveSubscriptionInfo or null if no active subscription exists
     */
    suspend fun getActiveSubscription(userId: String, apiKey: String): Resource<ActiveSubscriptionInfo?>

    /**
     * Processes a purchase transaction.
     * @param request The purchase request containing transaction details
     * @param apiKey API key for authentication
     * @return Boolean indicating whether the purchase was successfully handled
     */
    suspend fun handlePurchase(request: HandlePurchaseRequest, apiKey: String): Boolean
}