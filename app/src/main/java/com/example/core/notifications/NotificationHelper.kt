package com.example.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

object NotificationHelper {
    const val CHANNEL_BUDGET_ALERTS = "goldledger_budget_alerts"
    const val CHANNEL_RECURRING = "goldledger_recurring_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when spending reaches budget thresholds"
                enableVibration(true)
            }

            val recurringChannel = NotificationChannel(
                CHANNEL_RECURRING,
                "Recurring Payments",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled recurring transactions"
            }

            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(recurringChannel)
        }
    }

    fun showBudgetNotification(context: Context, id: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.goldledger_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    fun showRecurringNotification(context: Context, id: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(R.drawable.goldledger_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
