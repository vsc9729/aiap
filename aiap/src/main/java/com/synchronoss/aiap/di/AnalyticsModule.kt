package com.synchronoss.aiap.di

import android.app.Application
import com.synchronoss.aiap.core.data.repository.analytics.LocalyticsManagerImpl
import com.synchronoss.aiap.core.domain.repository.analytics.LocalyticsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    
    @Provides
    @Singleton
    fun provideLocalyticsManager(application: Application): LocalyticsManager {
        return LocalyticsManagerImpl(application).apply {
            initialize()
        }
    }
} 
