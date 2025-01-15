package com.synchronoss.aiap.di

class PurchaseUpdateHandler(var onPurchaseUpdated: () -> Unit, var onPurchaseStarted: () -> Unit) {
    fun handlePurchaseUpdate() {
        onPurchaseUpdated()
    }
    fun handlePurchaseStarted() {
        onPurchaseStarted()
    }
}