package com.example.util

import android.util.Log

/**
 * Diagnostic logger for Samsung Galaxy J1 (Android 5.1.1) and general debugging.
 * Uses a consistent log tag 'StorageManagerApp' suitable for `adb logcat -s StorageManagerApp`.
 */
object AppLog {
    private const val TAG = "StorageManagerApp"

    fun d(stage: String, message: String) {
        Log.d(TAG, "[$stage] $message")
    }

    fun i(stage: String, message: String) {
        Log.i(TAG, "[$stage] $message")
    }

    fun w(stage: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(TAG, "[$stage] $message", throwable)
        } else {
            Log.w(TAG, "[$stage] $message")
        }
    }

    fun e(stage: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "[$stage] $message", throwable)
        } else {
            Log.e(TAG, "[$stage] $message")
        }
    }
}
