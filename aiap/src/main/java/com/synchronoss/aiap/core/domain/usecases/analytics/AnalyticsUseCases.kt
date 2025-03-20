package com.synchronoss.aiap.core.domain.usecases.analytics

import com.synchronoss.aiap.core.domain.repository.analytics.AnalyticsManager
import javax.inject.Inject

/**
 * Collection of use cases for managing analytics operations.
 * Ensures Analytics is initialized.
 *
 * @property analyticsManager The AnalyticsManager instance
 */
class AnalyticsUseCases @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    fun initialize() {
        // Initialize Analytics
        analyticsManager.initialize()
    }

    fun track(
        eventName: String,
        properties: Map<String, Any> = emptyMap()
    ) {
        analyticsManager.track(eventName, properties)
    }
} 