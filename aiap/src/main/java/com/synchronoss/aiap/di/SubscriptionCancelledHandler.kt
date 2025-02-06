package com.synchronoss.aiap.di

class SubscriptionCancelledHandler(var onSubscriptionCancelled: () -> Unit) {
    fun handleSubscriptionCancelled() {
        onSubscriptionCancelled()
    }
}