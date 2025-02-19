package com.synchronoss.aiap.core.di

import android.content.Context
import com.synchronoss.aiap.utils.CacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing caching-related dependencies.
 * Provides singleton instance of cache manager for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    /**
     * Provides a singleton instance of CacheManager.
     * @param context The application context
     * @return A new instance of CacheManager initialized with the application context
     */
    @Provides
    @Singleton
    fun provideCacheManager(
        @ApplicationContext context: Context
    ): CacheManager {
        return CacheManager(context)
    }
} 