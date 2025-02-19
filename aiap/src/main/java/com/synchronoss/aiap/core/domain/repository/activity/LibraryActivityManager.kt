package com.synchronoss.aiap.core.domain.repository.activity

/**
 * Interface for managing library activity operations.
 * Handles launching and cleaning up library-related activities.
 */
interface LibraryActivityManager {
    /**
     * Launches the library activity.
     */
    fun launchLibrary()

    /**
     * Performs cleanup operations for the library activity.
     */
    fun cleanup()
}