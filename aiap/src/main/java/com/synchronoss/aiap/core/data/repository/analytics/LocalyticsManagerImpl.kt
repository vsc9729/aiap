package com.synchronoss.aiap.core.data.repository.analytics

import android.app.Application
import com.localytics.androidx.Localytics
import com.synchronoss.aiap.core.domain.repository.analytics.LocalyticsManager
import javax.inject.Inject

class LocalyticsManagerImpl @Inject constructor(
    private val application: Application
) : LocalyticsManager {

    override fun initialize() {
        Localytics.integrate(application)
        Localytics.setLoggingEnabled(true)
    }

    override fun trackEvent(eventName: String, attributes: Map<String, String>) {
        Localytics.tagEvent(eventName, attributes)
    }

    override fun tagScreen(screenName: String) {
        Localytics.tagScreen(screenName)
    }

    override fun setCustomDimension(dimensionIndex: Int, value: String?) {
        Localytics.setCustomDimension(dimensionIndex, value)
    }

    override fun setProfileAttribute(attribute: String, value: String) {
        Localytics.setProfileAttribute(attribute, value)
    }

    override fun uploadEvents() {
        Localytics.upload()
    }
} 