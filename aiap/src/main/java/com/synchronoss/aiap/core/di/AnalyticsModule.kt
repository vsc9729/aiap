package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.core.data.repository.analytics.LocalyticsAnalyticsManagerImpl
import com.synchronoss.aiap.core.domain.repository.analytics.AnalyticsManager
import com.synchronoss.aiap.core.domain.usecases.analytics.AnalyticsUseCases
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger module for providing analytics-related dependencies.
 * Provides singleton instances of analytics components for dependency injection.
 */
@Module
object AnalyticsModule {

    /**
     * Provides a singleton instance of AnalyticsManager.
     * @param application The Android Application instance
     * @return A new instance of LocalyticsAnalyticsManagerImpl
     */
    @Provides
    @Singleton
    fun provideAnalyticsManager(
        application: Application
    ): AnalyticsManager {
        return LocalyticsAnalyticsManagerImpl(application)
    }

    /**
     * Provides a singleton instance of AnalyticsUseCases.
     * @param analyticsManager The AnalyticsManager instance
     * @return A new instance of AnalyticsUseCases
     */
    @Provides
    @Singleton
    fun provideAnalyticsUseCases(
        analyticsManager: AnalyticsManager
    ): AnalyticsUseCases {
        return AnalyticsUseCases(analyticsManager)
    }
} 