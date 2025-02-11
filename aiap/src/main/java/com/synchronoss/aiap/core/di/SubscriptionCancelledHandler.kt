package com.synchronoss.aiap.core.di

class SubscriptionCancelledHandler(var onSubscriptionCancelled: () -> Unit) {
    fun handleSubscriptionCancelled() {
        onSubscriptionCancelled()
    }
}