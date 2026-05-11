package com.subscriptiontracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.subscriptiontracker.MainActivity
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.formatFull
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repo = SubscriptionRepository(db.subscriptionDao(), db.paymentHistoryDao())

        val today = LocalDate.now()
        val dueSoon = repo.getDueWithinDays(1)
            .filter { it.deadlineDate == today.plusDays(1) && it.status == SubscriptionStatus.ACTIVE }

        if (dueSoon.isEmpty()) return Result.success()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        createChannel(manager)

        dueSoon.forEach { sub ->
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                data = android.net.Uri.parse("subscriptiontracker://detail/${sub.id}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                applicationContext,
                sub.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("即将到期: ${sub.name}")
                .setContentText("${formatCurrency(sub.amount)} · ${sub.deadlineDate.formatFull()}")
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()

            manager.notify(sub.id.toInt(), notification)
        }

        return Result.success()
    }

    private fun createChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "订阅到期提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "提醒即将到期的订阅"
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "subscription_due"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_due_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
