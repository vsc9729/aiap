package com.synchronoss.aiap.di

import com.synchronoss.aiap.domain.repository.product.ProductManager
import com.synchronoss.aiap.domain.usecases.product.ProductManagerUseCases
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ProductModuleTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var productManager: ProductManager
    
    @Inject
    lateinit var productManagerUseCases: ProductManagerUseCases

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testProvideProductManager() {
        assertNotNull(productManager)
    }

    @Test
    fun testProvideProductManagerUseCases() {
        assertNotNull(productManagerUseCases)
        assertNotNull(productManagerUseCases.getProductsApi)
        assertNotNull(productManagerUseCases.getActiveSubscription)
    }
}