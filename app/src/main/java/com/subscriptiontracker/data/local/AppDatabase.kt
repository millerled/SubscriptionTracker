package com.subscriptiontracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.subscriptiontracker.data.local.converter.Converters
import com.subscriptiontracker.data.local.dao.PaymentHistoryDao
import com.subscriptiontracker.data.local.dao.SubscriptionDao
import com.subscriptiontracker.data.local.entity.PaymentHistoryEntity
import com.subscriptiontracker.data.local.entity.SubscriptionEntity

@Database(
    entities = [SubscriptionEntity::class, PaymentHistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao

    companion object {
        private const val DATABASE_NAME = "subscription_tracker_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
