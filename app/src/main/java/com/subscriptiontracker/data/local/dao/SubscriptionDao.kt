package com.subscriptiontracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subscriptiontracker.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY deadlineDate ASC")
    fun getAllByDeadline(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun getAllByDateAdded(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllByCustomOrder(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE status = :status ORDER BY deadlineDate ASC")
    fun getByStatus(status: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE deadlineDate >= :todayEpoch AND deadlineDate <= :expiringEpoch")
    suspend fun getDueWithinDays(todayEpoch: Long, expiringEpoch: Long): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("SELECT COALESCE(MIN(sortOrder), 0) FROM subscriptions")
    suspend fun getMinSortOrder(): Int

    @Query("UPDATE subscriptions SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("""
        SELECT * FROM subscriptions
        WHERE (createdAt >= :startMillis AND createdAt <= :endMillis)
           OR (modifiedAt >= :startMillis AND modifiedAt <= :endMillis AND modifiedAt > 0)
    """)
    suspend fun getActiveInRange(startMillis: Long, endMillis: Long): List<SubscriptionEntity>
}
