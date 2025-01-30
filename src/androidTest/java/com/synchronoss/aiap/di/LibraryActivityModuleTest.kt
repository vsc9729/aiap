package com.synchronoss.aiap.di

import android.app.Application
import com.synchronoss.aiap.domain.repository.activity.LibraryActivityManager
import com.synchronoss.aiap.domain.usecases.activity.LibraryActivityManagerUseCases
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LibraryActivityModuleTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var libraryActivityManager: LibraryActivityManager
    
    @Inject
    lateinit var libraryActivityManagerUseCases: LibraryActivityManagerUseCases

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testProvideLibraryActivityManager() {
        assertNotNull(libraryActivityManager)
    }

    @Test
    fun testProvideLibraryActivityManagerUseCases() {
        assertNotNull(libraryActivityManagerUseCases)
        assertNotNull(libraryActivityManagerUseCases.launchLibrary)
    }
}
