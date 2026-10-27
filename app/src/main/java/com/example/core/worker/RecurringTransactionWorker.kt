package com.example.core.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.notifications.NotificationHelper
import com.example.core.utilities.FinancialUtils
import com.example.data.local.GoldLedgerDatabase
import com.example.data.model.NotificationEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.TransactionEntity
import com.example.data.preferences.AppPreferencesManager
import com.example.data.repository.FinancialRepository
import java.util.Calendar

class RecurringTransactionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = GoldLedgerDatabase.getDatabase(applicationContext)
            val repository = FinancialRepository(database)
            val prefs = AppPreferencesManager(applicationContext)
            val currency = prefs.getCurrency()

            val now = System.currentTimeMillis()
            val dueList = repository.getDueRecurring(now)

            for (item in dueList) {
                // Execute transaction
                val tx = TransactionEntity(
                    type = item.type,
                    amountMinorUnits = item.amountMinorUnits,
                    accountId = item.accountId,
                    categoryId = item.categoryId,
                    dateTimestamp = now,
                    timeString = FinancialUtils.formatTime(now),
                    description = item.description,
                    isRecurring = true,
                    recurringId = item.id
                )
                repository.addTransaction(tx)

                // Compute next execution date
                val cal = Calendar.getInstance()
                cal.timeInMillis = item.nextExecutionDate
                when (item.frequency) {
                    RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    RecurringFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                    RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                }

                val updatedRecurring = item.copy(
                    nextExecutionDate = cal.timeInMillis,
                    lastExecutionDate = now
                )
                repository.updateRecurring(updatedRecurring)

                // Post in-app and system notification
                val formattedAmt = FinancialUtils.formatCurrency(item.amountMinorUnits, currency)
                val notifTitle = "Recurring ${item.type.name.lowercase().replaceFirstChar { it.uppercase() }} Processed"
                val notifMsg = "${item.description}: $formattedAmt processed automatically."

                repository.insertNotification(
                    NotificationEntity(
                        title = notifTitle,
                        message = notifMsg,
                        timestamp = now,
                        type = "RECURRING_PROCESSED"
                    )
                )

                NotificationHelper.showRecurringNotification(
                    applicationContext,
                    item.id.toInt(),
                    notifTitle,
                    notifMsg
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
