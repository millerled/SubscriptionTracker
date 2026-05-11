package com.subscriptiontracker

import android.app.Application
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.worker.NotificationWorker

class SubscriptionTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationWorker.schedule(this)
    }

    companion object {
        lateinit var instance: SubscriptionTrackerApp
            private set
    }
}
