package com.synchronoss.aiap.di

import android.app.Application
import com.synchronoss.aiap.data.remote.product.ProductApi
import com.synchronoss.aiap.domain.repository.billing.BillingManager
import com.synchronoss.aiap.domain.usecases.billing.BillingManagerUseCases
import com.synchronoss.aiap.utils.CacheManager
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class BillingModuleTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var billingManager: BillingManager
    
    @Inject
    lateinit var billingManagerUseCases: BillingManagerUseCases

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testProvideBillingManager() {
        assertNotNull(billingManager)
    }

    @Test
    fun testProvideBillingManagerUseCases() {
        assertNotNull(billingManagerUseCases)
        assertNotNull(billingManagerUseCases.startConnection)
        assertNotNull(billingManagerUseCases.purchaseSubscription)
        assertNotNull(billingManagerUseCases.checkExistingSubscription)
    }
}
