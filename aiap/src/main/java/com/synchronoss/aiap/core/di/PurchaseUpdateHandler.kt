package com.synchronoss.aiap.core.di

class PurchaseUpdateHandler(var onPurchaseUpdated: () -> Unit, var onPurchaseStarted: () -> Unit, var onPurchaseFailed: () -> Unit) {
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
}