package com.synchronoss.aiap.core.di

import android.app.Application
import com.synchronoss.aiap.core.data.remote.product.ProductApi
import com.synchronoss.aiap.core.data.repository.billing.BillingManagerImpl
import com.synchronoss.aiap.core.domain.handlers.PurchaseUpdateHandler
import com.synchronoss.aiap.core.domain.repository.billing.BillingManager
import com.synchronoss.aiap.core.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.core.domain.usecases.billing.CheckExistingSubscription
import com.synchronoss.aiap.core.domain.usecases.billing.GetProductDetails
import com.synchronoss.aiap.core.domain.usecases.billing.PurchaseSubscription
import com.synchronoss.aiap.core.domain.usecases.billing.StartConnection
import com.synchronoss.aiap.core.domain.usecases.product.ProductManagerUseCases

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    @Provides
    @Singleton
    fun providePurchaseUpdateHandler(): PurchaseUpdateHandler {
        return PurchaseUpdateHandler(
            onPurchaseUpdated = { /* Do nothing */ },
            onPurchaseStarted = { /* Do nothing */ },
            onPurchaseFailed = { /* Do nothing */ },
            onPurchaseStopped = { /* Do nothing */ }
        )
    }

    @Provides
    @Singleton
    fun provideBillingManager(
        application: Application,
        productManagerUseCases: ProductManagerUseCases,
        purchaseUpdateHandler: PurchaseUpdateHandler
    ): BillingManager {
        return BillingManagerImpl(application, productManagerUseCases, purchaseUpdateHandler)
    }

    @Provides
    @Singleton
    fun provideBillingManagerUseCases(billingManager: BillingManager): BillingManagerUseCases {
        return BillingManagerUseCases(
            startConnection = StartConnection(billingManager),
            purchaseSubscription = PurchaseSubscription(billingManager),
            checkExistingSubscription = CheckExistingSubscription(billingManager),
            getProductDetails = GetProductDetails(billingManager)
        )
    }
}