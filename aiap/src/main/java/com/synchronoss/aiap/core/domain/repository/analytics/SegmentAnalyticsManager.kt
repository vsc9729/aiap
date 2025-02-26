package com.synchronoss.aiap.core.domain.repository.analytics

/**
 * Interface for managing Segment analytics operations.
 */
interface SegmentAnalyticsManager {
    /**
     * Initializes the Segment analytics service.
     */
    fun initialize()

    /**
     * Tracks a custom event with associated properties.
     * @param eventName The name of the event to track
     * @param properties Map of key-value pairs containing event properties
     */
    fun track(eventName: String, properties: Map<String, Any> = emptyMap())
} 