package com.synchronoss.aiap.di

class PurchaseUpdateHandler(var onPurchaseUpdated: () -> Unit) {
    fun handlePurchaseUpdate() {
        onPurchaseUpdated()
    }
}