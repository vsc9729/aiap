package com.synchronoss.aiap.core.domain.usecases.activity

import androidx.activity.ComponentActivity
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager

/**
 * Use case for launching the library activity.
 * @property libraryActivityManager The library activity manager instance
 */
class LaunchLibrary(
    private val libraryActivityManager: LibraryActivityManager
) {
    /**
     * Launches the library activity.
     * @param activity The ComponentActivity that hosts the subscription modal
     */
    operator fun invoke(activity: ComponentActivity) {
        libraryActivityManager.launchLibrary(activity)
    }
}
