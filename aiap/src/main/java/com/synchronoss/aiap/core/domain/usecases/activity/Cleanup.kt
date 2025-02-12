package com.synchronoss.aiap.core.domain.usecases.activity


import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager


class Cleanup(
    private val libraryActivityManager: LibraryActivityManager
) {
    operator fun invoke(
    ) {
        libraryActivityManager.cleanup()
    }
}
