package com.synchronoss.aiap.di

import android.content.Context
import com.synchronoss.aiap.utils.CacheManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class CacheModuleTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var cacheManager: CacheManager

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testProvideCacheManager() {
        assertNotNull(cacheManager)
    }
}
