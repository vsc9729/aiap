package com.synchronoss.aiap.core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.CacheManager
import android.content.Context
import com.synchronoss.aiap.core.data.repository.theme.ThemeManagerImpl
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.core.domain.usecases.theme.GetThemeApi
import com.synchronoss.aiap.core.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.utils.Constants
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Dagger module for providing theme-related dependencies.
 * Provides singleton instances of theme management components.
 */
@Module
object ThemeModule {
    /**
     * Provides a singleton instance of ThemeManager.
     * @param context The application context
     * @return A new instance of ThemeManagerImpl
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        context: Context,
    ): ThemeManager {
        return ThemeManagerImpl(context)
    }

    /**
     * Provides a singleton instance of ThemeManagerUseCases.
     * @param themeManager The ThemeManager instance
     * @return A new instance of ThemeManagerUseCases with all required use cases
     */
    @Provides
    @Singleton
    fun provideThemeManagerUseCases(themeManager: ThemeManager): ThemeManagerUseCases {
        return ThemeManagerUseCases(
            getThemeApi = GetThemeApi(themeManager)
        )
    }

    /**
     * Provides a singleton instance of ThemeLoader.
     * @param themeManagerUseCases The ThemeManagerUseCases instance
     * @return A new instance of ThemeLoader
     */
    @Provides
    @Singleton
    fun provideThemeLoader(
        themeManagerUseCases: ThemeManagerUseCases,
    ): ThemeLoader {
        return ThemeLoader(themeManagerUseCases)
    }
}