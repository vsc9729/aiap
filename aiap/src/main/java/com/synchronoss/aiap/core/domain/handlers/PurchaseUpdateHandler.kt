package com.synchronoss.aiap.core.domain.handlers

/**
 * Handler class for managing purchase-related events and updates.
 * Provides callback mechanisms for different purchase states.
 *
 * @property onPurchaseUpdated Callback invoked when a purchase is updated
 * @property onPurchaseStarted Callback invoked when a purchase is initiated
 * @property onPurchaseFailed Callback invoked when a purchase fails
 * @property onPurchaseStopped Callback invoked when a purchase is stopped
 */
class PurchaseUpdateHandler(
    var onPurchaseUpdated: () -> Unit,
    var onPurchaseStarted: () -> Unit,
    var onPurchaseFailed: () -> Unit,
    var onPurchaseStopped: () -> Unit
) {
    /**
     * Flag indicating whether the purchase was launched via an intent.
     */
    var isLaunchedViaIntent: Boolean = false
    
    /**
     * Handles purchase update events by invoking the onPurchaseUpdated callback.
     */
    fun handlePurchaseUpdate() {
        onPurchaseUpdated()
    }
    
    /**
     * Handles purchase started events by invoking the onPurchaseStarted callback.
     */
    fun handlePurchaseStarted() {
        onPurchaseStarted()
    }
    
    /**
     * Handles purchase failed events by invoking the onPurchaseFailed callback.
     */
    fun handlePurchaseFailed() {
        onPurchaseFailed()
    }

    /**
     * Handles purchase stopped events by invoking the onPurchaseStopped callback.
     */
    fun handlePurchaseStopped() {
        onPurchaseStopped()
    }
} 