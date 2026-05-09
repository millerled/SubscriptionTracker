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

    @Query("SELECT * FROM subscriptions ORDER BY status, deadlineDate ASC")
    fun getAllByStatus(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY deadlineDate ASC")
    fun getAllByDeadline(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun getAllByDateAdded(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllByCustomOrder(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)
}
