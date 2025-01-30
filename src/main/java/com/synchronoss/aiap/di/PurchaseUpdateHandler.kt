package com.synchronoss.aiap.di

class PurchaseUpdateHandler(var onPurchaseUpdated: () -> Unit, var onPurchaseStarted: () -> Unit, var onPurchaseFailed: () -> Unit) {
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