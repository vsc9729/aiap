package com.synchronoss.aiap.domain.usecases.activity

import com.synchronoss.aiap.domain.repository.activity.LibraryActivityManager

class LaunchLibrary(
    private val libraryActivityManager: LibraryActivityManager
) {
    operator fun invoke(
    ) {
        libraryActivityManager.launchLibrary()
    }
}
