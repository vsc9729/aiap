package com.synchronoss.aiap.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.synchronoss.aiap.data.remote.theme.ThemeApi
import com.synchronoss.aiap.data.repository.theme.ThemeManagerImpl
import com.synchronoss.aiap.domain.repository.theme.ThemeManager
import com.synchronoss.aiap.domain.usecases.theme.GetThemeApi
import com.synchronoss.aiap.domain.usecases.theme.ThemeManagerUseCases
import com.synchronoss.aiap.ui.theme.ThemeLoader
import com.synchronoss.aiap.utils.CacheManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    fun provideThemeApi(): ThemeApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://sync-api.blr0.geekydev.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        return retrofit.create(ThemeApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideThemeMapper(): ThemeMapper {
//        return ThemeMapper()
//    }

    @Provides
    @Singleton
    fun provideThemeManager(api: ThemeApi,): ThemeManager {
        return ThemeManagerImpl(api)
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
        cacheManager: CacheManager
    ): ThemeLoader {
        return ThemeLoader(themeManagerUseCases, cacheManager)
    }

}