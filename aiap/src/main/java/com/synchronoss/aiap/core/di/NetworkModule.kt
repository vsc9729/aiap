package com.synchronoss.aiap.core.di

import android.content.Context
import com.synchronoss.aiap.utils.NetworkConnectionListener
import com.synchronoss.aiap.utils.ToastService
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Dagger module that provides network-related dependencies.
 */
@Module
class NetworkModule {

    /**
     * Provides a CoroutineScope for background operations.
     */
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    /**
     * Provides the ToastService singleton.
     */
    @Provides
    @Singleton
    fun provideToastService(context: Context, coroutineScope: CoroutineScope): ToastService {
        return ToastService(context, coroutineScope)
    }

    /**
     * Provides the NetworkConnectionListener singleton.
     */
    @Provides
    @Singleton
    fun provideNetworkConnectionListener(
        context: Context,
        toastService: ToastService
    ): NetworkConnectionListener {
        return NetworkConnectionListener(context, toastService)
    }
} 