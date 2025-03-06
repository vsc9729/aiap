package com.synchronoss.aiap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.synchronoss.aiap.R
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * A listener that monitors network connectivity changes and shows a toast when the connection is lost.
 */
@Singleton
class NetworkConnectionListener @Inject constructor(
    private val context: Context,
    private val toastService: ToastService
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isConnected = false
    private val TAG = "NetworkConnectionListener"

    // Network callback to monitor connectivity changes
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            LogUtils.d(TAG, "Network available")
            val wasConnected = isConnected
            isConnected = true
            
            // Only show reconnected toast if we were previously disconnected
            if (!wasConnected) {
                toastService.showToast(
                    isSuccess = true,
                    heading = context.getString(R.string.connection_restored_title),
                    message = context.getString(R.string.connection_restored_message)
                )
            }
        }

        override fun onLost(network: Network) {
            LogUtils.d(TAG, "Network lost")
            isConnected = false
            toastService.showToast(
                heading = context.getString(R.string.no_connection_title),
                message = context.getString(R.string.no_connection_message)
            )
        }
    }

    /**
     * Registers the network callback to start monitoring connectivity changes.
     * Returns a boolean indicating if the network is currently available.
     */
    suspend fun register(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            // Check initial connection state
            isConnected = isNetworkAvailable()
            
            // Register for all network changes
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            LogUtils.d(TAG, "Network callback registered")
            
            continuation.resume(isConnected)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to register network callback", e)
            continuation.resume(false)
        }
        
        continuation.invokeOnCancellation {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                LogUtils.d(TAG, "Network callback unregistered due to cancellation")
            } catch (e: Exception) {
                LogUtils.e(TAG, "Failed to unregister network callback on cancellation", e)
            }
        }
    }

    /**
     * Unregisters the network callback to stop monitoring connectivity changes.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            LogUtils.d(TAG, "Network callback unregistered")
        } catch (e: Exception) {
            LogUtils.e(TAG, "Failed to unregister network callback", e)
        }
    }

    /**
     * Checks if the network is currently available.
     */
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
} 