package com.synchronoss.aiap.core.data.repository.activity

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.synchronoss.aiap.utils.LogUtils

/**
 * Implementation of LibraryActivityManager interface that handles library activity operations.
 * Manages lifecycle events and subscription updates for the library activity.
 *
 * @property context Android application context
 * @property subscriptionCancelledHandler Handler for subscription cancellation events
 */
class LibraryActivityManagerImpl(
    private val context: Context,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : LibraryActivityManager {

    private var isInitialized = false
    private lateinit var lifecycleObserver: LibraryLifecycleObserver

    /**
     * Launches the library activity and initializes if necessary.
     */
    override fun launchLibrary() {
        if (!isInitialized) {
            initialize()
        }
    }

    /**
     * Initializes the library activity manager.
     * Sets up lifecycle observer for background/foreground transitions.
     */
    private fun initialize() {
        lifecycleObserver = LibraryLifecycleObserver(
            libraryActivityManagerImpl = this
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        isInitialized = true
    }

    /**
     * Performs cleanup by removing lifecycle observer.
     */
    override fun cleanup() {
        if (isInitialized) {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
            isInitialized = false
        }
    }

    /**
     * Refreshes subscription status.
     * Called when the app returns to foreground.
     */
    internal fun refreshSubscriptions() {
        try {
            subscriptionCancelledHandler.handleSubscriptionCancelled()
        } catch (e: Exception) {
            LogUtils.e("LibraryActivityManager", "Failed to refresh subscriptions", e)
        }
    }
}

/**
 * Lifecycle observer for the library activity.
 * Handles background/foreground transitions and triggers subscription refresh.
 *
 * @property libraryActivityManagerImpl Reference to the library activity manager
 */
internal class LibraryLifecycleObserver(
    private val libraryActivityManagerImpl: LibraryActivityManagerImpl
) : DefaultLifecycleObserver {
    private var isInBackground = false

    /**
     * Called when the app goes to background.
     */
    override fun onStop(owner: LifecycleOwner) {
        isInBackground = true
    }

    /**
     * Called when the app returns to foreground.
     * Triggers subscription refresh if returning from background.
     */
    override fun onStart(owner: LifecycleOwner) {
        if (isInBackground) {
            libraryActivityManagerImpl.refreshSubscriptions()
            isInBackground = false
        }
    }
}