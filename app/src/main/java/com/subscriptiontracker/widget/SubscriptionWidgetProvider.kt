package com.subscriptiontracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.subscriptiontracker.MainActivity
import com.subscriptiontracker.R
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.formatShort
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class SubscriptionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_subscription_list)

        try {
            val db = AppDatabase.getInstance(context)
            val repo = SubscriptionRepository(db.subscriptionDao(), db.paymentHistoryDao())

            val today = LocalDate.now()
            val upcoming = runBlocking {
                repo.getDueWithinDays(7)
                    .filter { it.status == SubscriptionStatus.ACTIVE }
                    .sortedBy { it.deadlineDate }
                    .take(5)
            }

            if (upcoming.isEmpty()) {
                val row = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
                row.setTextViewText(android.R.id.text1, "近期无到期订阅")
                views.addView(R.id.widget_container, row)
            } else {
                upcoming.forEach { sub ->
                    val row = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
                    row.setTextViewText(
                        android.R.id.text1,
                        "${sub.name}  ${formatCurrency(sub.amount)}  ${sub.deadlineDate.formatShort()}"
                    )
                    val intent = Intent(context, MainActivity::class.java).apply {
                        data = Uri.parse("subscriptiontracker://detail/${sub.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pending = PendingIntent.getActivity(
                        context,
                        sub.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    row.setOnClickPendingIntent(android.R.id.text1, pending)
                    views.addView(R.id.widget_container, row)
                }
            }
        } catch (e: Exception) {
            val row = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
            row.setTextViewText(android.R.id.text1, "加载失败")
            views.addView(R.id.widget_container, row)
        }

        manager.updateAppWidget(widgetId, views)
    }
}
