package com.synchronoss.aiap.core.data.repository.activity

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.synchronoss.aiap.R
import com.synchronoss.aiap.core.domain.handlers.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager
import com.synchronoss.aiap.utils.LogUtils

/**
 * Implementation of LibraryActivityManager interface that handles library activity operations.
 * Manages lifecycle events and subscription updates for the subscription modal.
 *
 * @property context Android application context
 * @property subscriptionCancelledHandler Handler for subscription cancellation events
 */
class LibraryActivityManagerImpl(
    private val context: Context,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : LibraryActivityManager {

    private var isInitialized = false
    private var currentActivity: ComponentActivity? = null
    private val lifecycleObserver by lazy {
        LibraryLifecycleObserver(this)
    }

    /**
     * Launches the library activity and initializes if necessary.
     * @param activity The ComponentActivity that hosts the subscription modal
     */
    override fun launchLibrary(activity: ComponentActivity) {
        currentActivity = activity
        if (!isInitialized) {
            initialize()
        }
    }

    /**
     * Initializes the library activity manager.
     * Sets up lifecycle observer for the activity's lifecycle.
     */
    private fun initialize() {
        currentActivity?.lifecycle?.addObserver(lifecycleObserver)
        isInitialized = true
    }

    /**
     * Performs cleanup by removing lifecycle observer.
     */
    override fun cleanup() {
        if (isInitialized) {
            currentActivity?.lifecycle?.removeObserver(lifecycleObserver)
            currentActivity = null
            isInitialized = false
        }
    }

    /**
     * Refreshes subscription status.
     * Called when the activity returns to foreground.
     */
    internal fun refreshSubscriptions() {
        try {
            subscriptionCancelledHandler.handleSubscriptionCancelled()
        } catch (e: IllegalStateException) {
            LogUtils.e(TAG, context.getString(R.string.subscription_handler_not_initialized), e)
        } catch (e: Exception) {
            LogUtils.e(TAG, context.getString(R.string.subscription_refresh_failed), e)
        }
    }

    companion object {
        private const val TAG = "LibraryActivityManager"
    }
}

/**
 * Lifecycle observer for the subscription modal.
 * Handles activity resume/pause transitions and triggers subscription refresh.
 *
 * @property libraryActivityManagerImpl Reference to the library activity manager
 */
internal class LibraryLifecycleObserver(
    private val libraryActivityManagerImpl: LibraryActivityManagerImpl
) : DefaultLifecycleObserver {
    private var isPaused = false
    private var isInBackground = false

    /**
     * Called when the activity is stopped (goes to background).
     */
    override fun onStop(owner: LifecycleOwner) {
        isInBackground = true
    }

    /**
     * Called when the activity is paused.
     */
    override fun onPause(owner: LifecycleOwner) {
        isPaused = true
    }

    /**
     * Called when the activity resumes.
     * Triggers subscription refresh only if returning from background.
     */
    override fun onResume(owner: LifecycleOwner) {
        if (isPaused && isInBackground) {
            libraryActivityManagerImpl.refreshSubscriptions()
            isInBackground = false
        }
        isPaused = false
    }
}