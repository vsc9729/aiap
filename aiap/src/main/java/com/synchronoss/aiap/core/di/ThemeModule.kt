package com.synchronoss.aiap.core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.CacheManager
import android.content.Context
import com.synchronoss.aiap.core.data.repository.theme.ThemeManagerImpl
import com.synchronoss.aiap.core.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.core.domain.usecases.theme.GetThemeFile
import com.synchronoss.aiap.core.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.theme.TransformThemeData
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
     * @param transformThemeData The TransformThemeData use case
     * @return A new instance of ThemeManagerImpl
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        context: Context,
        transformThemeData: TransformThemeData
    ): ThemeManager {
        return ThemeManagerImpl(context, transformThemeData = transformThemeData)
    }

    /**
     * Provides a singleton instance of ThemeManagerUseCases.
     * @param themeManager The ThemeManager instance
     * @param transformThemeData The TransformThemeData use case
     * @return A new instance of ThemeManagerUseCases with all required use cases
     */
    @Provides
    @Singleton
    fun provideThemeManagerUseCases(
        themeManager: ThemeManager,
        transformThemeData: TransformThemeData
    ): ThemeManagerUseCases {
        return ThemeManagerUseCases(
            getThemeFile = GetThemeFile(themeManager),
            transformThemeData = transformThemeData
        )
    }

    /**
     * Provides a singleton instance of ThemeLoader.
     * @param themeManagerUseCases The ThemeManagerUseCases instance
     * @param context The application context
     * @return A new instance of ThemeLoader
     */
    @Provides
    @Singleton
    fun provideThemeLoader(
        themeManagerUseCases: ThemeManagerUseCases,
        context: Context
    ): ThemeLoader {
        return ThemeLoader(themeManagerUseCases, context)
    }
}