package com.synchronoss.aiap.core.data.repository.activity

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.synchronoss.aiap.core.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.core.domain.repository.activity.LibraryActivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryActivityManagerImpl(
    private val context: Context,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
) : LibraryActivityManager {

    private var isInitialized = false
    private lateinit var lifecycleObserver: LibraryLifecycleObserver

    override fun launchLibrary() {
        if (!isInitialized) {
            initialize()
        }
    }

    private fun initialize() {
        lifecycleObserver = LibraryLifecycleObserver(
            libraryActivityManagerImpl = this
        )
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        isInitialized = true
    }

    internal fun refreshSubscriptions() {
        try {
                subscriptionCancelledHandler.handleSubscriptionCancelled()
            } catch (e: Exception) {
                Log.e("LibraryManager", "Failed to refresh subscriptions", e)
            }
    }
}

internal class LibraryLifecycleObserver(
    private val libraryActivityManagerImpl: LibraryActivityManagerImpl
) : DefaultLifecycleObserver {
    private var isInBackground = false

    override fun onStop(owner: LifecycleOwner) {
        isInBackground = true
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isInBackground) {
            libraryActivityManagerImpl.refreshSubscriptions()
            isInBackground = false
        }
    }
}