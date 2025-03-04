package com.synchronoss.aiap.utils

import android.content.Context
import android.util.Log
import com.localytics.androidx.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for standardized logging across the app
 */
object LogUtils {
    private const val APP_TAG = "AIAP"
    private const val MAX_TAG_LENGTH = 23 // Android's maximum tag length
    private const val LOG_FILE_NAME = "aiap_logs.txt"
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private var logFile: File? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    // Use the app's data directory
                    val logDir = File(context.applicationContext.getExternalFilesDir(null), "logs")
                    if (!logDir.exists()) {
                        logDir.mkdirs()
                    }
                    
                    logFile = File(logDir, LOG_FILE_NAME).also { file ->
                        if (!file.exists()) {
                            try {
                                file.createNewFile()
                            } catch (e: Exception) {
                                Log.e(createTag("LogUtils"), "Failed to create log file", e)
                            }
                        }
                    }
                    isInitialized = true
                }
            }
        }
    }

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
        val tag = createTag(className)
            Log.d(tag, message)
        writeToFile("DEBUG", tag, message)
    }

    fun i(className: String, message: String) {
        val tag = createTag(className)
        Log.i(tag, message)
        writeToFile("INFO", tag, message)
    }

    fun w(className: String, message: String) {
        val tag = createTag(className)
        Log.w(tag, message)
        writeToFile("WARN", tag, message)
    }

    fun e(className: String, message: String, throwable: Throwable? = null) {
        val tag = createTag(className)
        if (throwable != null) {
            Log.e(tag, message, throwable)
            writeToFile("ERROR", tag, "$message\n${throwable.stackTraceToString()}")
        } else {
            Log.e(tag, message)
            writeToFile("ERROR", tag, message)
        }
    }

    @Synchronized
    private fun writeToFile(level: String, tag: String, message: String) {
        logFile?.let { file ->
            try {
                // Check file size and rotate if necessary
                if (file.length() > MAX_FILE_SIZE_BYTES) {
                    rotateLogFile()
                }

                // Format: [2024-03-04 12:34:56.789] [LEVEL] [TAG] Message
                val timestamp = dateFormat.format(Date())
                val logEntry = "[$timestamp] [$level] [$tag] $message\n"

                file.appendText(logEntry)
            } catch (e: Exception) {
                Log.e(createTag("LogUtils"), "Failed to write to log file", e)
            }
        }
    }

    private fun rotateLogFile() {
        logFile?.let { file ->
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupFile = File(file.parent, "${LOG_FILE_NAME}_$timestamp")
                file.renameTo(backupFile)
                file.createNewFile()
            } catch (e: Exception) {
                Log.e(createTag("LogUtils"), "Failed to rotate log file", e)
            }
        }
    }

    fun getLogFile(): File? = logFile

    fun clearLogs() {
        logFile?.let { file ->
            try {
                file.writeText("")
            } catch (e: Exception) {
                Log.e(createTag("LogUtils"), "Failed to clear logs", e)
            }
        }
    }
}