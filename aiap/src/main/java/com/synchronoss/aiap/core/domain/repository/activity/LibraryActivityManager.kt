package com.synchronoss.aiap.core.domain.repository.activity

import androidx.activity.ComponentActivity

/**
 * Interface for managing library activity operations.
 * Handles launching and cleaning up subscription modal-related activities.
 */
interface LibraryActivityManager {
    /**
     * Launches the library activity.
     * @param activity The ComponentActivity that hosts the subscription modal
     */
    fun launchLibrary(activity: ComponentActivity)

    /**
     * Performs cleanup operations for the subscription modal.
     */
    fun cleanup()
}