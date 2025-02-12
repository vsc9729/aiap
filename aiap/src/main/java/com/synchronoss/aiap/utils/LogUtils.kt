package com.synchronoss.aiap.utils

import android.util.Log
import com.localytics.androidx.BuildConfig

/**
 * Utility class for standardized logging across the app
 */
object LogUtils {
    private const val APP_TAG = "AIAP"
    private const val MAX_TAG_LENGTH = 23 // Android's maximum tag length

    /**
     * Creates a standardized tag for logging
     * @param className The name of the class doing the logging
     * @return A properly formatted tag string
     */
    private fun createTag(className: String): String {
        val tag = "$APP_TAG:$className"
        return if (tag.length > MAX_TAG_LENGTH) tag.substring(0, MAX_TAG_LENGTH) else tag
    }

    fun d(className: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(createTag(className), message)
        }
    }

    fun i(className: String, message: String) {
        Log.i(createTag(className), message)
    }

    fun w(className: String, message: String) {
        Log.w(createTag(className), message)
    }

    fun e(className: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(createTag(className), message, throwable)
        } else {
            Log.e(createTag(className), message)
        }
    }
} 