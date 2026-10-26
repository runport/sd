package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val CHANNEL_ID = "j1_storage_reminder_channel"
    private const val CHANNEL_NAME = "یادآوری حافظه گوشی"
    private const val PREFS_NAME = "j1_storage_prefs"
    private const val KEY_REMINDER_ENABLED = "key_reminder_enabled"

    fun isDailyReminderEnabled(context: Context): Boolean {
        return try {
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sp.getBoolean(KEY_REMINDER_ENABLED, true)
        } catch (t: Throwable) {
            AppLog.w("NotificationHelper", "Failed to read reminder preference", t)
            true
        }
    }

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        try {
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sp.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
            AppLog.i("NotificationHelper", "Daily reminder preference set to $enabled")
        } catch (t: Throwable) {
            AppLog.w("NotificationHelper", "Failed to save reminder preference", t)
        }
    }

    fun showStorageAlertNotification(context: Context, title: String, message: String) {
        AppLog.i("NotificationHelper", "Posting storage alert notification")
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "هشدار و یادآوری فضای ذخیره‌سازی برای جلوگیری از کندی دستگاه"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_storage)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(101, notification)
            AppLog.i("NotificationHelper", "Notification posted successfully")
        } catch (t: Throwable) {
            AppLog.e("NotificationHelper", "Failed to post storage alert notification", t)
        }
    }
}
