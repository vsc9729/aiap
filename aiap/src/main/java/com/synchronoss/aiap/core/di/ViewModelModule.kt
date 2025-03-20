package com.synchronoss.aiap.core.di

import android.app.Application
import android.content.Context
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.usecases.activity.LibraryActivityManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.analytics.AnalyticsUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.presentation.viewmodels.SubscriptionsViewModelFactory
import com.synchronoss.aiap.ui.theme.ThemeLoader
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ViewModelModule {
    
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application
    
    @Provides
    @Singleton
    fun provideSubscriptionsViewModelFactory(
        billingManagerUseCases: BillingManagerUseCases,
        productManagerUseCases: ProductManagerUseCases,
        themeLoader: ThemeLoader,
        libraryActivityManagerUseCases: LibraryActivityManagerUseCases,
        purchaseUpdateHandler: PurchaseUpdateHandler,
        subscriptionCancelledHandler: SubscriptionCancelledHandler,
        analyticsUseCases: AnalyticsUseCases,
        context: Context
    ): SubscriptionsViewModelFactory {
        return SubscriptionsViewModelFactory(
            billingManagerUseCases,
            productManagerUseCases,
            themeLoader,
            libraryActivityManagerUseCases,
            purchaseUpdateHandler,
            subscriptionCancelledHandler,
            analyticsUseCases,
            context
        )
    }
} 