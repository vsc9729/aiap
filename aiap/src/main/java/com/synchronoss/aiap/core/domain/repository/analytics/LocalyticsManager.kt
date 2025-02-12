package com.synchronoss.aiap.core.domain.repository.analytics

interface LocalyticsManager {
    fun initialize()
    fun trackEvent(eventName: String, attributes: Map<String, String>)
    fun tagScreen(screenName: String)
    fun setCustomDimension(dimensionIndex: Int, value: String?)
    fun setProfileAttribute(attribute: String, value: String)
    fun uploadEvents()
} 