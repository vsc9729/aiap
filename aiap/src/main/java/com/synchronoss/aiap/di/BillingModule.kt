package com.synchronoss.aiap.di

import android.app.Application
import com.synchronoss.aiap.data.remote.ProductApi
import com.synchronoss.aiap.data.repository.billing.BillingManagerImpl
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.PurchaseSubscription
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.domain.usecases.billing.CheckExistingSubscription
import com.synchronoss.aiap.domain.usecases.billing.StartConnection


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
        )
    }
    @Provides
    @Singleton
    fun provideBillingManager(
        application: Application,
        productApi: ProductApi,
        purchaseUpdateHandler: PurchaseUpdateHandler
    ): BillingManager {
        return BillingManagerImpl(application, productApi, purchaseUpdateHandler)
    }

    @Provides
    @Singleton
    fun provideBillingManagerUseCases(billingManager: BillingManager): BillingManagerUseCases {
        return BillingManagerUseCases(
            startConnection = StartConnection(billingManager),
            purchaseSubscription = PurchaseSubscription(billingManager),
            checkExistingSubscription = CheckExistingSubscription(billingManager)
        )
    }

}