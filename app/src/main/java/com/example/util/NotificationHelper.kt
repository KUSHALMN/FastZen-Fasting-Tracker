package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    const val CHANNEL_ID = "fastzen_notifications"
    const val CHANNEL_NAME = "FastZen Reminders & Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Fasting stage milestones, goal completion alerts, and hydration reminders"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun sendTestNotification(context: Context): Boolean {
        return showNotification(
            context,
            title = "FastZen Notification Active",
            message = "Notifications are working! You'll receive alerts for fasting milestones and hydration.",
            notificationId = 1001
        )
    }

    fun sendGoalReachedNotification(context: Context, hours: Int): Boolean {
        return showNotification(
            context,
            title = "🎉 Fasting Target Achieved!",
            message = "Congratulations! You completed your ${hours}h fast. You can safely break your fast or extend it.",
            notificationId = 1002
        )
    }

    fun sendStageReachedNotification(context: Context, stageName: String, summary: String): Boolean {
        return showNotification(
            context,
            title = "⚡ Fasting Milestone: $stageName",
            message = summary,
            notificationId = 1003
        )
    }

    fun sendHydrationReminderNotification(context: Context): Boolean {
        return showNotification(
            context,
            title = "💧 Time to Hydrate",
            message = "Stay hydrated during your fast! Drink a glass of water or herbal tea.",
            notificationId = 1004
        )
    }
}
