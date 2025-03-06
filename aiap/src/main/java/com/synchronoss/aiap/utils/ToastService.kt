package com.synchronoss.aiap.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.synchronoss.aiap.presentation.state.ToastState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class responsible for managing toast notifications.
 * This class is designed to be used by both the ViewModel and NetworkConnectionListener.
 */
@Singleton
class ToastService @Inject constructor(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "ToastService"
        private const val DEFAULT_TOAST_DURATION_MS = 3000L
    }

    // Toast state that can be observed by UI components
    var toastState by mutableStateOf(ToastState())
        private set

    // Job for auto-dismissing toasts
    private var toastJob: Job? = null

    /**
     * Shows a toast with the given heading and message.
     *
     * @param heading The heading text for the toast
     * @param message The message text for the toast
     * @param isSuccess Whether this is a success toast (affects styling)
     * @param isPending Whether this is a pending toast (affects styling)
     * @param formatArgs Optional formatting arguments for the message
     */
    fun showToast(
        heading: String,
        message: String,
        isSuccess: Boolean = false,
        isPending: Boolean = false,
        formatArgs: Any? = null
    ) {
        LogUtils.d(TAG, "Showing toast: $heading - $message")
        
        // Cancel any existing toast job
        toastJob?.cancel()
        
        // Update toast state
        toastState = ToastState(
            isVisible = true,
            heading = heading,
            message = message,
            isSuccess = isSuccess,
            isPending = isPending,
            formatArgs = formatArgs
        )
        
        // Auto-dismiss non-success and non-pending toasts after a delay
        if (!isSuccess && !isPending) {
            toastJob = coroutineScope.launch {
                delay(DEFAULT_TOAST_DURATION_MS)
                hideToast()
            }
        }
    }

    /**
     * Hides the currently displayed toast.
     */
    fun hideToast() {
        toastJob?.cancel()
        toastState = toastState.copy(isVisible = false)
    }
} 