package com.synchronoss.aiap.core.data.repository.analytics

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.destinations.localytics.LocalyticsDestination
import com.segment.analytics.kotlin.destinations.localytics.LocalyticsSettings
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.repository.analytics.SegmentAnalyticsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SegmentAnalyticsManagerImpl @Inject constructor(
    private val application: Application
) : SegmentAnalyticsManager {

    companion object {
        private const val TAG = "SegmentAnalyticsManagerImpl"
        private const val SEGMENT_WRITE_KEY = "HuySlE4jHKpNgAvTCeNwViXFVkdqxSzp"
        @Volatile
        private var isInitialized = false
        private lateinit var analytics: Analytics
    }

    /**
     * Thread-safe initialization of Segment analytics service.
     */
    override fun initialize() {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    try {
                        // Initialize Analytics with basic configuration as per the documentation
                        analytics = Analytics(SEGMENT_WRITE_KEY, application) {
                            this.flushAt = 3
                            this.trackApplicationLifecycleEvents = true
//                            this.debug = true // Enable debug mode for more logging
                        }
                        
                        // Add Localytics as a device-mode destination as per the documentation
                        analytics.add(plugin = LocalyticsDestination())
                        
                        isInitialized = true
            
                        
                        // Send a test event to verify connection
                        track("SegmentInitialized", mapOf("timestamp" to System.currentTimeMillis()))
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
                
                analytics.track(eventName, properties)
                
            } catch (e: Exception) {
                
            }
        }
    }

    /**
     * Ensures Segment Analytics is initialized before executing an operation
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