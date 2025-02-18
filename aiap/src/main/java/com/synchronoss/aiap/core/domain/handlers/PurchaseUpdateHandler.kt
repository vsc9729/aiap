package com.synchronoss.aiap.core.domain.handlers

class PurchaseUpdateHandler(
    var onPurchaseUpdated: () -> Unit,
    var onPurchaseStarted: () -> Unit,
    var onPurchaseFailed: () -> Unit,
    var onPurchaseStopped: () -> Unit
) {
    var isLaunchedViaIntent: Boolean = false
    
    fun handlePurchaseUpdate() {
        onPurchaseUpdated()
    }
    
    fun handlePurchaseStarted() {
        onPurchaseStarted()
    }
    
    fun handlePurchaseFailed() {
        onPurchaseFailed()
    }

    fun handlePurchaseStopped() {
        onPurchaseStopped()
    }
} 