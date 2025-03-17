package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.repository.billing.BillingManagerImpl
import com.synchronoss.aiap.core.domain.handlers.DefaultPurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.usecases.billing.*
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger module for providing billing-related dependencies.
 * Provides singleton instances of billing components for dependency injection.
 */
@Module
abstract class BillingModule {
    /**
     * Binds the default implementation of PurchaseUpdateHandler.
     */
    @Binds
    @Singleton
    abstract fun bindPurchaseUpdateHandler(impl: DefaultPurchaseUpdateHandler): PurchaseUpdateHandler

    companion object {
        /**
         * Provides a default implementation of PurchaseUpdateHandler.
         */
        @Provides
        @Singleton
        fun provideDefaultPurchaseUpdateHandler(): DefaultPurchaseUpdateHandler {
            return DefaultPurchaseUpdateHandler(
                onPurchaseUpdated = { },
                onPurchaseStarted = { },
                onPurchaseFailed = { },
                onPurchaseStopped = { }
            )
        }

        /**
         * Provides a singleton instance of BillingManager.
         * @param application The Android Application instance
         * @param productManagerUseCases Use cases for product management
         * @param purchaseUpdateHandler Handler for purchase updates
         * @return A new instance of BillingManagerImpl
         */
        @Provides
        @Singleton
        fun provideBillingManager(
            application: Application,
            productManagerUseCases: ProductManagerUseCases,
            purchaseUpdateHandler: PurchaseUpdateHandler
        ): BillingManager {
            return BillingManagerImpl(application, productManagerUseCases, purchaseUpdateHandler)
        }

        /**
         * Provides a singleton instance of BillingManagerUseCases.
         * @param billingManager The BillingManager instance
         * @return A new instance of BillingManagerUseCases with all required use cases
         */
        @Provides
        @Singleton
        fun provideBillingManagerUseCases(billingManager: BillingManager): BillingManagerUseCases {
            return BillingManagerUseCases(
                startConnection = StartConnection(billingManager),
                purchaseSubscription = PurchaseSubscription(billingManager),
                checkExistingSubscription = CheckExistingSubscription(billingManager),
                handleUnacknowledgedPurchases = HandleUnacknowledgedPurchases(billingManager),
                getProductDetails = GetProductDetails(billingManager)
            )
        }
    }
}