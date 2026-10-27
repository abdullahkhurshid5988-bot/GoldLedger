package com.example

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.ads.AdManager
import com.example.core.notifications.NotificationHelper
import com.example.core.worker.RecurringTransactionWorker
import java.util.concurrent.TimeUnit

class GoldLedgerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize AdMob with test IDs
        AdManager.initialize(this)

        // 2. Setup Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // 3. Schedule Recurring Transactions Worker via Android WorkManager
        scheduleRecurringWorker()
    }

    private fun scheduleRecurringWorker() {
        try {
            val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                repeatInterval = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "GoldLedgerRecurringWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
