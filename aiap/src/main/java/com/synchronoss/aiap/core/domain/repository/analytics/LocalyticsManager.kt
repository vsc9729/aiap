package com.synchronoss.aiap.core.domain.repository.analytics

/**
 * Interface for managing analytics operations using Localytics.
 * Provides functionality for tracking events, screens, and user attributes.
 */
interface LocalyticsManager {
    /**
     * Initializes the Localytics analytics service.
     */
    fun initialize()

    /**
     * Tracks a custom event with associated attributes.
     * @param eventName The name of the event to track
     * @param attributes Map of key-value pairs containing event attributes
     */
    fun trackEvent(eventName: String, attributes: Map<String, String>)

    /**
     * Tags a screen view for analytics tracking.
     * @param screenName The name of the screen being viewed
     */
    fun tagScreen(screenName: String)

    /**
     * Sets a custom dimension for analytics segmentation.
     * @param dimensionIndex The index of the custom dimension
     * @param value The value to set for the dimension
     */
    fun setCustomDimension(dimensionIndex: Int, value: String?)

    /**
     * Sets a profile attribute for the current user.
     * @param attribute The name of the profile attribute
     * @param value The value to set for the attribute
     */
    fun setProfileAttribute(attribute: String, value: String)

    /**
     * Uploads pending analytics events to the server.
     */
    fun uploadEvents()
} 