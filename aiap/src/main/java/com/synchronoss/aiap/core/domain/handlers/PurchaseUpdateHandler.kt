package com.synchronoss.aiap.core.domain.handlers

/**
 * Interface defining the contract for handling purchase-related events and updates.
 */
interface PurchaseUpdateHandler {
    /**
     * Flag indicating whether the purchase was launched via an intent.
     */
    var isLaunchedViaIntent: Boolean

    /**
     * Handles purchase update events.
     */
    fun handlePurchaseUpdate()

    /**
     * Handles purchase started events.
     */
    fun handlePurchaseStarted()

    /**
     * Handles purchase failed events.
     */
    fun handlePurchaseFailed()

    /**
     * Handles purchase stopped events.
     */
    fun handlePurchaseStopped()

    /**
     * Callback for when a purchase is updated.
     */
    var onPurchaseUpdated: () -> Unit

    /**
     * Callback for when a purchase is started.
     */
    var onPurchaseStarted: () -> Unit

    /**
     * Callback for when a purchase fails.
     */
    var onPurchaseFailed: () -> Unit

    /**
     * Callback for when a purchase is stopped.
     */
    var onPurchaseStopped: () -> Unit
}

/**
 * Default implementation of PurchaseUpdateHandler.
 */
class DefaultPurchaseUpdateHandler(
    override var onPurchaseUpdated: () -> Unit,
    override var onPurchaseStarted: () -> Unit,
    override var onPurchaseFailed: () -> Unit,
    override var onPurchaseStopped: () -> Unit
) : PurchaseUpdateHandler {
    override var isLaunchedViaIntent: Boolean = false

    override fun handlePurchaseUpdate() {
        onPurchaseUpdated()
    }

    override fun handlePurchaseStarted() {
        onPurchaseStarted()
    }

    override fun handlePurchaseFailed() {
        onPurchaseFailed()
    }

    override fun handlePurchaseStopped() {
        onPurchaseStopped()
    }
} 