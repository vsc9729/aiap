package com.synchronoss.aiap.core.data.repository.analytics

import android.app.Application
import com.localytics.androidx.Localytics
import com.synchronoss.aiap.core.domain.repository.analytics.AnalyticsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalyticsAnalyticsManagerImpl @Inject constructor(
    private val application: Application
) : AnalyticsManager {

    companion object {
        private const val TAG = "LocalyticsAnalyticsManagerImpl"
        private const val LOCALYTICS_KEY = "380d11b3eed719179a5bfc1-dd8a5da2-e31b-11ef-9059-007c928ca240"
        @Volatile
        private var isInitialized = false
    }

    /**
     * Thread-safe initialization of Localytics analytics service.
     */
    override fun initialize() {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    try {
                        // Initialize Localytics without push notifications
                        // Removed registerPush() call as we don't need push functionality
                        Localytics.setOption("ll_app_key", LOCALYTICS_KEY)
                        Localytics.autoIntegrate(application)
                        
                        isInitialized = true
                        
                        // Send a test event to verify connection
                        track("LocalyticsInitialized", mapOf("timestamp" to System.currentTimeMillis()))
                    } catch (e: Exception) {
                        throw e // Re-throw to prevent incomplete initialization
                    }
                }
            }
        }
    }

    /**
     * Tracks a custom event with associated properties.
     * @param eventName Name of the event to track
     * @param properties Map of event properties
     */
    override fun track(eventName: String, properties: Map<String, Any>) {
        ensureInitialized {
            try {
                // Convert properties to attributes and dimensions if needed
                val attributes = properties.entries.associate { 
                    it.key to it.value.toString() 
                }
                
                // Track the event using Localytics
                Localytics.tagEvent(eventName, attributes)
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }

    /**
     * Ensures Localytics is initialized before executing an operation
     */
    private inline fun ensureInitialized(operation: () -> Unit) {
        if (!isInitialized) {
            initialize()
        }
        if (isInitialized) {
            operation()
        }
    }
} 