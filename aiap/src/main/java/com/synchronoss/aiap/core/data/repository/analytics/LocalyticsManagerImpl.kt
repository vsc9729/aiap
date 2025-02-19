package com.synchronoss.aiap.core.data.repository.analytics

import android.app.Application
import com.localytics.androidx.Localytics
import com.synchronoss.aiap.core.domain.repository.analytics.LocalyticsManager
import javax.inject.Inject

/**
 * Implementation of LocalyticsManager interface that handles analytics operations.
 * Manages initialization and tracking of analytics events using Localytics.
 *
 * @property application Android application instance
 */
class LocalyticsManagerImpl @Inject constructor(
    private val application: Application
) : LocalyticsManager {

    init {
        initialize()
    }

    /**
     * Initializes the Localytics analytics service.
     * Enables logging for debug purposes.
     */
    override fun initialize() {
        Localytics.integrate(application)
        Localytics.setLoggingEnabled(true)
    }

    /**
     * Tracks a custom event with associated attributes.
     * @param eventName Name of the event to track
     * @param attributes Map of event attributes
     */
    override fun trackEvent(eventName: String, attributes: Map<String, String>) {
        Localytics.tagEvent(eventName, attributes)
    }

    /**
     * Tags a screen view for analytics tracking.
     * @param screenName Name of the screen being viewed
     */
    override fun tagScreen(screenName: String) {
        Localytics.tagScreen(screenName)
    }

    /**
     * Sets a custom dimension for analytics segmentation.
     * @param dimensionIndex Index of the custom dimension
     * @param value Value to set for the dimension
     */
    override fun setCustomDimension(dimensionIndex: Int, value: String?) {
        Localytics.setCustomDimension(dimensionIndex, value)
    }

    /**
     * Sets a profile attribute for the current user.
     * @param attribute Name of the profile attribute
     * @param value Value to set for the attribute
     */
    override fun setProfileAttribute(attribute: String, value: String) {
        Localytics.setProfileAttribute(attribute, value)
    }

    /**
     * Uploads pending analytics events to the server.
     */
    override fun uploadEvents() {
        Localytics.upload()
    }
} 