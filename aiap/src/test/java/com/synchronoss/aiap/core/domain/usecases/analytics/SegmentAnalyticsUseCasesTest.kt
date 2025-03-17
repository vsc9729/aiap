package com.synchronoss.aiap.core.domain.usecases.analytics

import com.synchronoss.aiap.core.domain.repository.analytics.SegmentAnalyticsManager
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SegmentAnalyticsUseCasesTest {

    // Mock dependencies
    private val segmentAnalyticsManager: SegmentAnalyticsManager = mockk()
    
    // Class under test
    private lateinit var segmentAnalyticsUseCases: SegmentAnalyticsUseCases
    
    @Before
    fun setup() {
        justRun { segmentAnalyticsManager.initialize() }
        justRun { segmentAnalyticsManager.track(any(), any()) }
        
        segmentAnalyticsUseCases = SegmentAnalyticsUseCases(segmentAnalyticsManager)
    }
    
    @Test
    fun `initialize should call initialize on the analytics manager`() {
        // When
        segmentAnalyticsUseCases.initialize()
        
        // Then
        verify { segmentAnalyticsManager.initialize() }
    }
    
    @Test
    fun `track should call track on the analytics manager with event name and properties`() {
        // Given
        val eventName = "test_event"
        val properties = mapOf("key" to "value")
        
        // When
        segmentAnalyticsUseCases.track(eventName, properties)
        
        // Then
        verify { segmentAnalyticsManager.track(eventName, properties) }
    }
    
    @Test
    fun `track should call track with empty properties when no properties provided`() {
        // Given
        val eventName = "test_event"
        
        // When
        segmentAnalyticsUseCases.track(eventName)
        
        // Then
        verify { segmentAnalyticsManager.track(eventName, emptyMap()) }
    }
}
