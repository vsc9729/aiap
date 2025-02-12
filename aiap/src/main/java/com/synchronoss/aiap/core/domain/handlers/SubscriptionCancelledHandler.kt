package com.synchronoss.aiap.core.domain.handlers

class SubscriptionCancelledHandler(var onSubscriptionCancelled: () -> Unit) {
    fun handleSubscriptionCancelled() {
        onSubscriptionCancelled()
    }
} 