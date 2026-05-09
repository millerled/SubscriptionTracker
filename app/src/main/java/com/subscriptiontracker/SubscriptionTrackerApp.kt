package com.subscriptiontracker

import android.app.Application
import com.subscriptiontracker.data.local.AppDatabase

class SubscriptionTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
