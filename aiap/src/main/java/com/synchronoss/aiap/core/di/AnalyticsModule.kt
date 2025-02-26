package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.core.data.repository.analytics.SegmentAnalyticsManagerImpl
import com.synchronoss.aiap.core.domain.repository.analytics.SegmentAnalyticsManager
import com.synchronoss.aiap.core.domain.usecases.analytics.SegmentAnalyticsUseCases
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger module for providing analytics-related dependencies.
 * Provides singleton instances of Segment analytics components for dependency injection.
 */
@Module
object AnalyticsModule {

    /**
     * Provides a singleton instance of SegmentAnalyticsManager.
     * @param application The Android Application instance
     * @return A new instance of SegmentAnalyticsManagerImpl
     */
    @Provides
    @Singleton
    fun provideSegmentAnalyticsManager(
        application: Application
    ): SegmentAnalyticsManager {
        return SegmentAnalyticsManagerImpl(application)
    }

    /**
     * Provides a singleton instance of SegmentAnalyticsUseCases.
     * @param segmentAnalyticsManager The SegmentAnalyticsManager instance
     * @return A new instance of SegmentAnalyticsUseCases
     */
    @Provides
    @Singleton
    fun provideSegmentAnalyticsUseCases(
        segmentAnalyticsManager: SegmentAnalyticsManager
    ): SegmentAnalyticsUseCases {
        return SegmentAnalyticsUseCases(segmentAnalyticsManager)
    }
} 