package com.synchronoss.aiap.core.domain.usecases.activity

/**
 * Collection of use cases for managing library activity operations.
 *
 * @property launchLibrary Use case for launching the library activity
 * @property cleanup Use case for performing cleanup operations
 */
data class LibraryActivityManagerUseCases (
    val launchLibrary: LaunchLibrary,
    val cleanup: Cleanup
)