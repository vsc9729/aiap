package com.synchronoss.aiap.core.domain.usecases.analytics

import com.synchronoss.aiap.core.domain.repository.analytics.AnalyticsManager
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AnalyticsUseCasesTest {

    // Mock dependencies
    private val analyticsManager: AnalyticsManager = mockk()
    
    // Class under test
    private lateinit var analyticsUseCases: AnalyticsUseCases
    
    @Before
    fun setup() {
        justRun { analyticsManager.initialize() }
        justRun { analyticsManager.track(any(), any()) }
        
        analyticsUseCases = AnalyticsUseCases(analyticsManager)
    }
    
    @Test
    fun `initialize should call initialize on the analytics manager`() {
        // When
        analyticsUseCases.initialize()
        
        // Then
        verify { analyticsManager.initialize() }
    }
    
    @Test
    fun `track should call track on the analytics manager with event name and properties`() {
        // Given
        val eventName = "test_event"
        val properties = mapOf("key" to "value")
        
        // When
        analyticsUseCases.track(eventName, properties)
        
        // Then
        verify { analyticsManager.track(eventName, properties) }
    }
    
    @Test
    fun `track should call track with empty properties when no properties provided`() {
        // Given
        val eventName = "test_event"
        
        // When
        analyticsUseCases.track(eventName)
        
        // Then
        verify { analyticsManager.track(eventName, emptyMap()) }
    }
}
