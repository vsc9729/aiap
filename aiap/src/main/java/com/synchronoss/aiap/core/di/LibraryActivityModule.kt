package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.core.data.repository.activity.LibraryActivityManagerImpl
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager
import com.synchronoss.aiap.core.domain.usecases.activity.Cleanup
import com.synchronoss.aiap.core.domain.usecases.activity.LaunchLibrary
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object LibraryActivityModule {
    @Provides
    @Singleton
    fun provideSubscriptionCancelledHandler(): SubscriptionCancelledHandler {
        return SubscriptionCancelledHandler(
            onSubscriptionCancelled = { /* Do nothing */ },
        )
    }
    @Provides
    @Singleton
    fun provideLibraryActivityManager(
        application: Application,
        subscriptionCancelledHandler: SubscriptionCancelledHandler
    ): LibraryActivityManager {
        return LibraryActivityManagerImpl(application, subscriptionCancelledHandler)
    }

    @Provides
    @Singleton
    fun provideLibraryActivityManagerUseCases(
        libraryActivityManager: LibraryActivityManager
    ): LibraryActivityManagerUseCases {
        return LibraryActivityManagerUseCases(
            launchLibrary = LaunchLibrary(libraryActivityManager),
            cleanup = Cleanup(libraryActivityManager)
        )
    }

}