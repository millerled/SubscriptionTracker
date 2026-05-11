package com.subscriptiontracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.subscriptiontracker.data.local.converter.Converters
import com.subscriptiontracker.data.local.dao.PaymentHistoryDao
import com.subscriptiontracker.data.local.dao.SubscriptionDao
import com.subscriptiontracker.data.local.entity.PaymentHistoryEntity
import com.subscriptiontracker.data.local.entity.SubscriptionEntity

@Database(
    entities = [SubscriptionEntity::class, PaymentHistoryEntity::class],
    version = 3,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payment_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subscriptionId INTEGER NOT NULL,
                        paymentDate INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        action TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (subscriptionId) REFERENCES subscriptions(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_subscriptionId ON payment_history(subscriptionId)")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN autoRenew INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN wallpaperUri TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN startDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN modifiedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
