package com.synchronoss.aiap.core.domain.usecases.analytics

import com.synchronoss.aiap.core.domain.repository.analytics.SegmentAnalyticsManager
import javax.inject.Inject

/**
 * Collection of use cases for managing Segment analytics operations.
 * Ensures Segment Analytics is initialized.
 *
 * @property segmentAnalyticsManager The SegmentAnalyticsManager instance
 */
class SegmentAnalyticsUseCases @Inject constructor(
    private val segmentAnalyticsManager: SegmentAnalyticsManager
) {
    fun initialize() {
        // Initialize Segment Analytics
        segmentAnalyticsManager.initialize()
    }

    fun track(
        eventName: String,
        properties: Map<String, Any> = emptyMap()
    ) {
        segmentAnalyticsManager.track(eventName, properties)
    }
} 