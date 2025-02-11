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
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context,
    ): ThemeManager {
        return ThemeManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideThemeManagerUseCases(themeManager: ThemeManager): ThemeManagerUseCases {
        return ThemeManagerUseCases(
            getThemeApi = GetThemeApi(themeManager)
        )
    }

    @Provides
    @Singleton
    fun provideThemeLoader(
        themeManagerUseCases: ThemeManagerUseCases,
    ): ThemeLoader {
        return ThemeLoader(themeManagerUseCases)
    }

}