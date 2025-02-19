package com.synchronoss.aiap.core.domain.repository.billing

import androidx.activity.ComponentActivity
import com.android.billingclient.api.ProductDetails
import com.synchronoss.aiap.core.domain.models.ProductInfo
import kotlinx.coroutines.CompletableDeferred

/**
 * Interface for managing in-app billing operations.
 * Handles subscription purchases, checking existing subscriptions, and retrieving product details.
 */
interface BillingManager {
    /**
     * Establishes a connection to the billing service.
     * @return A CompletableDeferred that completes when the connection is established.
     */
    suspend fun startConnection(): CompletableDeferred<Unit>

    /**
     * Initiates a subscription purchase flow.
     * @param activity The activity from which the purchase flow is initiated
     * @param productDetails The details of the product to be purchased
     * @param onError Callback function to handle any errors during the purchase
     * @param userId The unique identifier of the user making the purchase
     * @param apiKey The API key required for the purchase transaction
     */
    suspend fun purchaseSubscription(
        activity: ComponentActivity,
        productDetails: ProductDetails,
        onError: (String) -> Unit,
        userId: String,
        apiKey: String
    )

    /**
     * Checks for any existing subscriptions for the current user.
     * @param onError Callback function to handle any errors during the check
     * @return The ProductDetails of the existing subscription, or null if none exists
     */
    suspend fun checkExistingSubscriptions(
        onError: (String) -> Unit
    ): ProductDetails?

    /**
     * Retrieves details for specified products.
     * @param productIds List of product identifiers to fetch details for
     * @param onError Callback function to handle any errors during the retrieval
     * @return List of ProductDetails for the requested products, or null if retrieval fails
     */
    suspend fun getProductDetails(
        productIds: List<String>,
        onError: (String) -> Unit
    ): List<ProductDetails>?
}