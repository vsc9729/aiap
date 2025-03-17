package com.synchronoss.aiap.di

import android.app.Application
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.domain.repository.activity.LibraryActivityManager
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.domain.usecases.billing.CheckExistingSubscription
import com.synchronoss.aiap.domain.usecases.billing.PurchaseSubscription
import com.synchronoss.aiap.domain.usecases.billing.StartConnection
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import com.synchronoss.aiap.utils.CacheManager

import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        BillingModule::class, 
        ProductModule::class, 
        CacheModule::class,
        LibraryActivityModule::class
    ]
)
object TestModule {
    @Provides
    @Singleton
    fun provideMockLibraryActivityManager(application: Application): LibraryActivityManager = mockk()

    @Provides
    @Singleton
    fun provideMockLibraryActivityManagerUseCases(
        libraryActivityManager: LibraryActivityManager
    ): LibraryActivityManagerUseCases {
        val mockUseCases = mockk<LibraryActivityManagerUseCases>()
        every { mockUseCases.launchLibrary } returns mockk()
        return mockUseCases
    }

    @Provides
    @Singleton
    fun provideMockProductApi(): ProductApi = mockk()

    @Provides
    @Singleton
    fun provideMockCacheManager(): CacheManager = mockk()

    @Provides
    @Singleton
    fun provideMockBillingManager(): BillingManager = mockk()

    @Provides
    @Singleton
    fun provideMockBillingManagerUseCases(billingManager: BillingManager): BillingManagerUseCases {
        return BillingManagerUseCases(
            startConnection = StartConnection(billingManager),
            purchaseSubscription = PurchaseSubscription(billingManager),
            checkExistingSubscription = CheckExistingSubscription(billingManager)
        )
    }

    @Provides
    @Singleton
    fun provideMockProductManager(
        api: ProductApi, 
        billingManager: BillingManager, 
        cacheManager: CacheManager
    ): ProductManager = mockk()

    @Provides
    @Singleton
    fun provideMockProductManagerUseCases(productManager: ProductManager): ProductManagerUseCases {
        val mockUseCases = mockk<ProductManagerUseCases>()
        every { mockUseCases.getProductsApi } returns mockk()
        every { mockUseCases.getActiveSubscription } returns mockk()
        return mockUseCases
    }

    @Provides
    @Singleton
    fun provideMockPurchaseUpdateHandler(): PurchaseUpdateHandler {
        return DefaultPurchaseUpdateHandler(
            onPurchaseUpdated = { /* Do nothing */ },
            onPurchaseStarted = { /* Do nothing */ },
            onPurchaseFailed = { /* Do nothing */ },
            onPurchaseStopped = { /* Do nothing */ }
        )
    }

    @Provides
    @Singleton
    fun provideMockSubscriptionCancelledHandler(): SubscriptionCancelledHandler {
        return SubscriptionCancelledHandler {
            /* Do nothing */
        }
    }
}