package com.synchronoss.aiap.core.domain.usecases.analytics

import com.synchronoss.aiap.core.domain.repository.analytics.LocalyticsManager
import javax.inject.Inject

class LocalyticsManagerUseCases @Inject constructor(
    private val localyticsManager: LocalyticsManager
) {
    fun trackSubscriptionEvent(
        eventName: String,
        productId: String,
        userId: String,
        additionalParams: Map<String, String> = emptyMap()
    ) {
        val attributes = mutableMapOf(
            "product_id" to productId,
            "user_id" to userId,
            "timestamp" to System.currentTimeMillis().toString()
        )
        attributes.putAll(additionalParams)
        localyticsManager.trackEvent(eventName, attributes)
    }

    fun trackScreenView(screenName: String) {
        localyticsManager.tagScreen(screenName)
    }

    fun setUserProfile(userId: String) {
        localyticsManager.setProfileAttribute("user_id", userId)
    }

    fun uploadPendingEvents() {
        localyticsManager.uploadEvents()
    }
} 