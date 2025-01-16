package com.synchronoss.aiap.data.repository.activity

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.synchronoss.aiap.di.SubscriptionCancelledHandler
import com.synchronoss.aiap.domain.repository.activity.LibraryActivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryActivityManagerImpl (
    private val context: Context,
    private val subscriptionCancelledHandler: SubscriptionCancelledHandler
): LibraryActivityManager{

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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                subscriptionCancelledHandler.handleSubscriptionCancelled()
            } catch (e: Exception) {
                Log.e("LibraryManager", "Failed to refresh subscriptions", e)
            }
        }
    }
}


internal class LibraryLifecycleObserver(
    private val libraryActivityManagerImpl: LibraryActivityManagerImpl
) : LifecycleObserver {
    private var isInBackground = false

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isInBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (isInBackground) {
            libraryActivityManagerImpl.refreshSubscriptions()
            isInBackground = false
        }
    }
}